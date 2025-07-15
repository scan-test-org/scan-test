package com.alibaba.apiopenplatform.service.gateway.factory;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author zh
 */
@Slf4j
public class HTTPClientFactory {

    public static RestTemplate createRestTemplate() {
        OkHttpClient okHttpClient = okHttpClient();
        // 使用OkHttp作为RestTemplate的底层客户端
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }

    public static OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .build();
    }

    public static void closeClient(RestTemplate restTemplate) {
        try {
            if (restTemplate != null) {
                ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
                if (factory instanceof OkHttp3ClientHttpRequestFactory) {
                    ((OkHttp3ClientHttpRequestFactory) factory).destroy();
                }
            }
        } catch (Exception e) {
            log.error("Error closing RestTemplate", e);
        }
    }
}