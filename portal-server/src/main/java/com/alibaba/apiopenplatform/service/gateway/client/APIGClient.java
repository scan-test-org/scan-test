package com.alibaba.apiopenplatform.service.gateway.client;

import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.gateway.APIGConfig;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.apig20240327.AsyncClient;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @author zh
 */
@Slf4j
public class APIGClient extends GatewayClient {

    private final AsyncClient apigClient;

    private final Gateway gateway;

    public APIGClient(Gateway gateway) {
        this.gateway = gateway;
        this.apigClient = createClient(gateway.getApigConfig());
    }

    @Override
    public String getGatewayId() {
        return gateway.getGatewayId();
    }

    @Override
    public boolean tryConnection() {
        try {
//            getRestAPIs();
            return true;
        } catch (Exception e) {
            log.warn("Connection test failed for APIG instance: {}", getGatewayId(), e);
            return false;
        }
    }

    @Override
    public void close() {
        if (apigClient != null) {
            apigClient.close();
        }
    }

    public <E> E execute(Function<AsyncClient, E> function) {
        try {
            return function.apply(apigClient);
        } catch (Exception e) {
            log.error("Error executing APIG request", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    private AsyncClient createClient(APIGConfig config) {
        // noinspection AklessInspection
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(config.getAccessKey())
                .accessKeySecret(config.getSecretKey())
                .build());

        return AsyncClient.builder()
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(String.format("apig.%s.aliyuncs.com", config.getRegion()))
                ).build();
    }
}