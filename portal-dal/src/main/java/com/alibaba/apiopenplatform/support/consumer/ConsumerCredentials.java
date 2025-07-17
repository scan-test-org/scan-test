package com.alibaba.apiopenplatform.support.consumer;

import lombok.Data;

/**
 * @author zh
 */
@Data
public class ConsumerCredentials {

    private JwtIdentityConfig jwtIdentityConfig;

    private HmacIdentityConfig hmacConfig;

    private ApiKeyIdentityConfig apiKeyIdentityConfig;
}
