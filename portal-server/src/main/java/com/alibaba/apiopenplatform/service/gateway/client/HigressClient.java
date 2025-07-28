package com.alibaba.apiopenplatform.service.gateway.client;

import com.alibaba.apiopenplatform.support.gateway.HigressConfig;
import com.alibaba.higress.sdk.config.HigressServiceConfig;
import com.alibaba.higress.sdk.service.HigressServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author zh
 */
@Slf4j
public class HigressClient extends GatewayClient {

//    private final RestTemplate restTemplate;

    private final HigressServiceProvider provider;

    public HigressClient(HigressConfig higressConfig) {
//        this.restTemplate = HTTPClientFactory.createRestTemplate();
        this.provider = createProvider(higressConfig);
    }


    @Override
    public void close() {
//        HTTPClientFactory.closeClient(restTemplate);
    }

//    private String createAuthHeader() {
//        // 实现认证逻辑
//        return "Bearer " + generateToken();
//    }

//    public <E> E execute(Function<RestTemplate, E> function) {
//        return function.apply(restTemplate);
//    }

    public <E> E execute(Function<HigressServiceProvider, E> function) {
        return function.apply(provider);
    }

    private HigressServiceProvider createProvider(HigressConfig config) {
        HigressServiceConfig serviceConfig = HigressServiceConfig.builder()
                .withControllerServiceHost(config.getHost())
                .withControllerServicePort(config.getPort())
                .withControllerAccessToken(config.getAccessToken())
                .withControllerJwtPolicy(config.getJwtPolicy())
                .build();

        try {
            return HigressServiceProvider.create(serviceConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}