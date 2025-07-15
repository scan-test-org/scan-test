package com.alibaba.apiopenplatform.config;

import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author zh
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(OkHttpClient okHttpClient) {
        // 使用OkHttp作为RestTemplate的底层客户端
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .build();
    }
}