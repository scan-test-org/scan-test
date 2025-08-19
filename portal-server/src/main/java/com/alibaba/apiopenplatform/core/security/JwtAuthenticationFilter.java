package com.alibaba.apiopenplatform.core.security;

import com.alibaba.apiopenplatform.auth.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author zxd
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        if (isPublicEndpoint(requestURI)) {
            chain.doFilter(request, response);
            return;
        }
        
        String token = extractToken(request);
        
        if (token != null) {
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Token已被列入黑名单");
                SecurityContextHolder.clearContext();
                unauthorized(response, "Token已失效");
                return;
            }
            try {
                processToken(token);
            } catch (Exception e) {
                log.warn("JWT认证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                unauthorized(response, "Token无效或已过期");
                return;
            }
        } else {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.equals("/admins/login") || 
               requestURI.equals("/developers/login") || 
               requestURI.equals("/developers") ||
               requestURI.equals("/admins/init") ||
               requestURI.equals("/admins/need-init") ||
               requestURI.equals("/developers/authorize") ||
               requestURI.equals("/developers/callback") ||
               requestURI.equals("/developers/providers") ||
               requestURI.equals("/favicon.ico") ||
               requestURI.equals("/error") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.startsWith("/portal/swagger-ui") ||
               requestURI.startsWith("/portal/v3/api-docs");
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("从Authorization头获取到token，长度: {}", token.length());
            return token;
        }
        
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    log.debug("从Cookie获取到auth_token，长度: {}", token != null ? token.length() : 0);
                    return token;
                }
            }
        }
        log.debug("未找到auth_token Cookie");
        return null;
    }

    private void processToken(String token) {
        Map<String, Object> claims = jwtService.parseAndValidateClaims(token);
        Object userIdObj = claims.get("userId");
        Object userTypeObj = claims.get("userType");
        if (!(userIdObj instanceof String) || !(userTypeObj instanceof String)) {
            log.warn("JWT缺少userId或userType或类型错误");
            throw new IllegalArgumentException("Token无效：缺少userId或userType");
        }
        String userId = (String) userIdObj;
        String userType = ((String) userTypeObj).toUpperCase();
        String role = "ROLE_" + userType;
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("JWT认证成功，userType={}, userId={}", userType, userId);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}");
    }
} 