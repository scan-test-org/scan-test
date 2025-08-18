package com.alibaba.apiopenplatform.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.service.PortalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zh
 */
@Slf4j
@RequiredArgsConstructor
public class PortalResolvingFilter extends OncePerRequestFilter {

    private final PortalService portalService;

    private final ContextHolder contextHolder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain)
            throws ServletException, IOException {
        try {
            String origin = request.getHeader("Origin");
            String host = request.getHeader("Host");
            String xForwardedHost = request.getHeader("X-Forwarded-Host");
            String xRealIp = request.getHeader("X-Real-IP");
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            
            String domain = null;
            if (origin != null) {
                try {
                    java.net.URI uri = new java.net.URI(origin);
                    domain = uri.getHost();
                } catch (Exception e) {
                    // 解析失败时可降级处理
                    domain = null;
                }
            }
            
            log.info("域名解析调试 - Origin: {}, Host: {}, X-Forwarded-Host: {}, ServerName: {}, X-Real-IP: {}, X-Forwarded-For: {}", 
                    origin, host, xForwardedHost, request.getServerName(), xRealIp, xForwardedFor);
            
            if (domain == null) {
                // 优先使用Host头，如果没有则使用ServerName
                if (host != null && !host.isEmpty()) {
                    domain = host.split(":")[0]; // 去掉端口号
                } else {
                    domain = request.getServerName();
                }
            }
            String portalId = portalService.resolvePortal(domain);

            if (StrUtil.isNotBlank(portalId)) {
                contextHolder.savePortal(portalId);
                log.info("Resolved portal for domain: {} with portalId: {}", domain, portalId);
            } else {
                log.info("No portal found for domain: {}", domain);
            }

            chain.doFilter(request, response);
        } finally {
            contextHolder.clearPortal();
        }
    }
}
