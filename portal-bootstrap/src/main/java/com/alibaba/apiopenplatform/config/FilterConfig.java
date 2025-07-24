package com.alibaba.apiopenplatform.config;

import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.filter.PortalResolvingFilter;
import com.alibaba.apiopenplatform.service.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * @author zh
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final PortalService portalService;

    private final ContextHolder contextHolder;

    @Bean
    public FilterRegistrationBean<PortalResolvingFilter> portalResolvingFilter() {
        FilterRegistrationBean<PortalResolvingFilter> registrationBean = new FilterRegistrationBean<>();

        PortalResolvingFilter filter = new PortalResolvingFilter(portalService, contextHolder);
        registrationBean.setFilter(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
