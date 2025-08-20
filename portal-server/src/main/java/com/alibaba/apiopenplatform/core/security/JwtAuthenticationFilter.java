package com.alibaba.apiopenplatform.core.security;

import com.alibaba.apiopenplatform.core.constant.Common;
import com.alibaba.apiopenplatform.core.utils.TokenUtil;
import com.alibaba.apiopenplatform.support.common.User;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

/**
 * @author zxd
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain)
            throws IOException, ServletException {

        try {
            String token = TokenUtil.getTokenFromRequest(request);
            if (token != null) {
                authenticateRequest(token);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.warn("认证处理异常: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }
        chain.doFilter(request, response);
    }

    private void authenticateRequest(String token) {
        User user = TokenUtil.parseUser(token);
        // 设置认证信息
        String role = Common.ROLE_PREFIX + user.getUserType().name();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUserId(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
} 