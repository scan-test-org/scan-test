package com.alibaba.apiopenplatform.support.consumer;

import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class ApiKeyIdentityConfig {

    private ApiKeySource apiKeySource;

    private List<Credential> credentials;
}


@Data
class ApiKeySource {
    private String source;
    private String value;
}

@Data
class Credential {
    private String generateMode;
    private String apikey;
}