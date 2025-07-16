package com.alibaba.apiopenplatform.support.gateway;

import lombok.Data;

/**
 * @author zh
 */
@Data
public class HigressConfig {

    private String host;

    private int port;

    private String accessToken;

    private String jwtPolicy;
}
