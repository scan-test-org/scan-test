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

    public String buildUniqueKey() {
        return String.format("%s:%s:%s", address, username, password);
    }
}
