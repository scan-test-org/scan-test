package com.alibaba.apiopenplatform.support;

import lombok.Data;

/**
 * @author zh
 */
@Data
public class OidcConfig {

    private String issuer;

    private String clientId;

    private String clientSecret;

    private String scopes;
}
