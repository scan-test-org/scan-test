package com.alibaba.apiopenplatform.support.consumer;

import lombok.Data;

/**
 * @author zh
 */
@Data
public class JwtIdentityConfig {

    private String type;

    private String secretType;

    private String jwks;

    private JwtTokenConfig jwtTokenConfig;

    private JwtPayloadConfig jwtPayloadConfig;
}


@Data
class JwtTokenConfig {
    private String position;
    private String key;
    private String prefix;
    private boolean pass;
}

@Data
class JwtPayloadConfig {
    private String payloadKeyName;
    private String payloadKeyValue;
}