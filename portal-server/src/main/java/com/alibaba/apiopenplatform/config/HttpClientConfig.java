package com.alibaba.apiopenplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP客户端配置
 * 用于OIDC认证中的HTTP请求
 */
@Configuration
public class HttpClientConfig {

    /**
     * RestTemplate Bean配置
     * 用于OIDC发现配置获取、令牌交换、用户信息获取等HTTP请求
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
