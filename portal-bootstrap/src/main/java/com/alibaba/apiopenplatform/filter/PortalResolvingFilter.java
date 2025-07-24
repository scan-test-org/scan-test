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
            String domain = request.getServerName();
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
