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
            log.info("zhao-origin-domain: {}, request domina:{}", domain, request.getServerName()
            );
            if (domain == null) {
                // 降级处理，比如用 getServerName()
                domain = request.getServerName();
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
