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
 * 通用JWT认证过滤器，支持管理员和开发者多用户类型
 * 根据claims动态注入ROLE_ADMIN/ROLE_DEVELOPER权限
 * 集成Token黑名单校验
 *
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
        // 对于登录和注册接口，跳过JWT验证
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/admins/login") || 
            requestURI.equals("/developers/login") || 
            requestURI.equals("/developers") ||
            requestURI.equals("/admins/init") ||
            requestURI.equals("/admins/need-init")) {
            chain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        if (token != null) {
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Token已被列入黑名单");
                unauthorized(response, "Token已失效");
                return;
            }
            try {
                Map<String, Object> claims = jwtService.parseAndValidateClaims(token);
                Object userIdObj = claims.get("userId");
                Object userTypeObj = claims.get("userType");
                if (!(userIdObj instanceof String) || !(userTypeObj instanceof String)) {
                    log.warn("JWT缺少userId或userType或类型错误");
                    unauthorized(response, "Token无效：缺少userId或userType");
                    return;
                }
                String userId = (String) userIdObj;
                String userType = ((String) userTypeObj).toUpperCase();
                String role = "ROLE_" + userType;
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JWT认证成功，userType={}, userId={}", userType, userId);
            } catch (Exception e) {
                log.warn("JWT认证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                unauthorized(response, "Token无效或已过期");
                return;
            }
        }
        chain.doFilter(request, response);
    }
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}");
    }
} 