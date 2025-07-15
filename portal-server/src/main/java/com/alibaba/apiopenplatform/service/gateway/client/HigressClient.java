package com.alibaba.apiopenplatform.service.gateway.client;

import com.alibaba.apiopenplatform.service.gateway.factory.HTTPClientFactory;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.gateway.HigressConfig;
//import com.alibaba.higress.sdk.config.HigressServiceConfig;
//import com.alibaba.higress.sdk.service.HigressServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author zh
 */
@Slf4j
public class HigressClient extends GatewayClient {

    private final Gateway instance;

    private final RestTemplate restTemplate;

    public HigressClient(Gateway instance) {
        this.instance = instance;
        this.restTemplate = HTTPClientFactory.createRestTemplate();
    }

    @Override
    public String getGatewayId() {
        return instance.getGatewayId();
    }

    @Override
    public boolean tryConnection() {
        try {
            return true;
//            ResponseEntity<String> response = restTemplate.getForEntity(
//                    buildUrl("/health"),
//                    String.class
//            );

//            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Connection test failed for Higress instance: {}", instance.getGatewayId(), e);
            return false;
        }
    }

    @Override
    public void close() {
        HTTPClientFactory.closeClient(restTemplate);
    }

//    private String createAuthHeader() {
//        // 实现认证逻辑
//        return "Bearer " + generateToken();
//    }

    public <E> E execute(Function<RestTemplate, E> function) {
        return function.apply(restTemplate);
    }

//    private HigressServiceProvider createProvider() {
//        HigressConfig config = instance.getHigressConfig();
//        HigressServiceConfig serviceConfig = HigressServiceConfig.builder()
//                .withControllerServiceHost(config.getHost())
//                .withControllerServicePort(config.getPort())
//                .withControllerAccessToken(config.getAccessToken())
//                .withControllerJwtPolicy(config.getJwtPolicy())
//                .build();
//
//        try {
//            return HigressServiceProvider.create(serviceConfig);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}