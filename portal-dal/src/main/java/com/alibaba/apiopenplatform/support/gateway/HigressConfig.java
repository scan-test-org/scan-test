package com.alibaba.apiopenplatform.support.gateway;

import com.alibaba.apiopenplatform.support.common.Encrypted;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class HigressConfig {

    private String address;

    private String username;

    @Encrypted
    private String password;

    private String host;

    private int port;

    private String accessToken;

    private String jwtPolicy;

    public String buildUniqueKey() {
        return String.format("%s:%s:%s", host, port, accessToken);
    }
}
