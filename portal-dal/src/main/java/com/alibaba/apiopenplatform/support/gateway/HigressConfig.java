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

    public String buildUniqueKey() {
        return String.format("%s:%s:%s", host, port, accessToken);
    }
}
