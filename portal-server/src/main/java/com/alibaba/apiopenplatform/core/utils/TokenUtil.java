package com.alibaba.apiopenplatform.core.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.alibaba.apiopenplatform.core.constant.Common;
import com.alibaba.apiopenplatform.support.common.User;
import com.alibaba.apiopenplatform.support.enums.UserType;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/***
 * @author zh
 */
public class TokenUtil {

    private static String JWT_SECRET;

    private static long JWT_EXPIRE_MILLIS;

    private static final Set<String> INVALID_TOKENS = ConcurrentHashMap.newKeySet();

    private static String getJwtSecret() {
        if (JWT_SECRET == null) {
            JWT_SECRET = SpringUtil.getProperty("jwt.secret");
        }

        if (StrUtil.isBlank(JWT_SECRET)) {
            throw new RuntimeException("JWT secret cannot be empty");
        }
        return JWT_SECRET;
    }

    private static long getJwtExpireMillis() {
        if (JWT_EXPIRE_MILLIS == 0) {
            String expiration = SpringUtil.getProperty("jwt.expiration");
            if (StrUtil.isBlank(expiration)) {
                throw new RuntimeException("JWT expiration is empty");
            }

            if (expiration.matches("\\d+[smhd]")) {
                JWT_EXPIRE_MILLIS = Duration.parse("PT" + expiration.toUpperCase()).toMillis();
            } else {
                JWT_EXPIRE_MILLIS = Long.parseLong(expiration);
            }
        }
        return JWT_EXPIRE_MILLIS;
    }

    public static String generateAdminToken(String userId) {
        return generateToken(UserType.ADMIN, userId);
    }

    public static String generateDeveloperToken(String userId) {
        return generateToken(UserType.DEVELOPER, userId);
    }

    /**
     * 生成令牌
     *
     * @param userType
     * @param userId
     * @return
     */
    private static String generateToken(UserType userType, String userId) {
        long now = System.currentTimeMillis();

        Map<String, String> claims = MapUtil.<String, String>builder()
                .put(Common.USER_TYPE, userType.name())
                .put(Common.USER_ID, userId)
                .build();

        return JWT.create()
                .addPayloads(claims)
                .setIssuedAt(new Date(now))
                .setExpiresAt(new Date(now + getJwtExpireMillis()))
                .setSigner(JWTSignerUtil.hs256(getJwtSecret().getBytes(StandardCharsets.UTF_8)))
                .sign();
    }

    /**
     * 解析Token
     *
     * @param token
     * @return
     */
    public static User parseUser(String token) {
        JWT jwt = JWTUtil.parseToken(token);

        // 验证签名
        boolean isValid = jwt.setSigner(JWTSignerUtil.hs256(getJwtSecret().getBytes(StandardCharsets.UTF_8))).verify();
        if (!isValid) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        // 验证过期时间
        Object expObj = jwt.getPayloads().get(JWT.EXPIRES_AT);
        if (ObjectUtil.isNotNull(expObj)) {
            long expireAt = Long.parseLong(expObj.toString());
            if (expireAt * 1000 <= System.currentTimeMillis()) {
                throw new IllegalArgumentException("Token has expired");
            }
        }

        return jwt.getPayloads().toBean(User.class);
    }

    public static String getTokenFromRequest(HttpServletRequest request) {
        // 从Header中获取token
        String authHeader = request.getHeader(Common.AUTHORIZATION_HEADER);

        String token = null;
        if (authHeader != null && authHeader.startsWith(Common.BEARER_PREFIX)) {
            token = authHeader.substring(Common.BEARER_PREFIX.length());
        }

        // 从Cookie中获取token
        if (StrUtil.isBlank(token)) {
            token = Optional.ofNullable(request.getCookies())
                    .flatMap(cookies -> Arrays.stream(cookies)
                            .filter(cookie -> Common.AUTH_TOKEN_COOKIE.equals(cookie.getName()))
                            .map(Cookie::getValue)
                            .findFirst())
                    .orElse(null);
        }
        if (StrUtil.isBlank(token) || INVALID_TOKENS.contains(token)) {
            return null;
        }

        return token;
    }

    public static void revokeToken(String token) {
        INVALID_TOKENS.add(token);
    }

    public static void revokeToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StrUtil.isNotBlank(token)) {
            revokeToken(token);
        }
    }

    public static boolean isTokenRevoked(String token) {
        return INVALID_TOKENS.contains(token);
    }
}
