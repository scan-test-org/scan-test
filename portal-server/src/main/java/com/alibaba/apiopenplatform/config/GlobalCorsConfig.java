package com.alibaba.apiopenplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局CORS跨域配置，允许前后端分离开发环境下的跨域请求
 * 支持所有来源、常用方法、所有请求头，允许带cookie
 *
 * @author zxd
 */
@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {
    /**
     * 配置全局跨域规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
} 