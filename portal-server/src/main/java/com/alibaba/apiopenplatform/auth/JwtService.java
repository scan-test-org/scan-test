package com.alibaba.apiopenplatform.auth;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

/**
 * JWT服务类，集中管理Token的生成、解析和验证，支持管理员和开发者两种用户类型
 *
 * @author zxd
 */
@Service
public class JwtService {
    private final String secret;
    private final long expireMillis;

    public JwtService(@Value("${jwt.secret}") String secret,
                     @Value("${jwt.expiration}") String expiration) {
        this.secret = secret;
        // 支持2h、7200000等格式
        if (expiration.matches("\\d+[smhd]")) {
            this.expireMillis = Duration.parse("PT" + expiration.toUpperCase()).toMillis();
        } else {
            this.expireMillis = Long.parseLong(expiration);
        }
    }

    /**
     * 生成JWT Token，支持管理员和开发者
     * @param userType 用户类型（admin/developer）
     * @param userId 用户唯一标识（adminId/developerId）
     * @param extraClaims 额外自定义claims（可选）
     * @return token字符串
     */
    public String generateToken(String userType, String userId, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userType", userType);
        claims.put("userId", userId);
        if (extraClaims != null) claims.putAll(extraClaims);
        return JWT.create()
                .addPayloads(claims)
                .setIssuedAt(new Date(now))
                .setExpiresAt(new Date(now + expireMillis))
                .setSigner(JWTSignerUtil.hs256(secret.getBytes()))
                .sign();
    }

    /**
     * 解析并验证Token，返回claims
     * @param token token字符串
     * @return claims Map
     * @throws IllegalArgumentException token无效或过期
     */
    public Map<String, Object> parseAndValidateClaims(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        boolean valid = jwt.setSigner(JWTSignerUtil.hs256(secret.getBytes())).verify();
        Object expObj = jwt.getPayloads().get("exp");
        boolean notExpired = true;
        if (expObj != null) {
            long exp = Long.parseLong(expObj.toString());
            notExpired = exp * 1000 > System.currentTimeMillis();
        }
        if (!valid || !notExpired) {
            throw new IllegalArgumentException("无效或过期的Token");
        }
        return jwt.getPayloads().toBean(Map.class);
    }

    /**
     * 只解析Token，不校验
     * （当前未被业务调用，如需可启用）
     */
    // public JWT parse(String token) {
    //     return JWTUtil.parseToken(token);
    // }

    /**
     * 生成带自定义过期时间的JWT Token（用于激活邮件等场景）
     * （当前未被业务调用，如需可启用）
     */
    // public String generateTokenWithCustomExpire(String userType, String userId, Map<String, Object> extraClaims, long expireMillis) {
    //     long now = System.currentTimeMillis();
    //     Map<String, Object> claims = new java.util.HashMap<>();
    //     claims.put("userType", userType);
    //     claims.put("userId", userId);
    //     if (extraClaims != null) claims.putAll(extraClaims);
    //     return JWT.create()
    //             .addPayloads(claims)
    //             .setIssuedAt(new Date(now))
    //             .setExpiresAt(new Date(now + expireMillis))
    //             .setSigner(JWTSignerUtil.hs256(secret.getBytes()))
    //             .sign();
    // }
} 