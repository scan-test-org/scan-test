package com.alibaba.apiopenplatform.support.gateway;

import com.alibaba.apiopenplatform.support.common.Encrypted;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class APIGConfig {

    @Encrypted
    private String accessKey;

    @Encrypted
    private String secretKey;

    private String region;

    public String buildUniqueKey() {
        return String.format("%s:%s:%s", accessKey, secretKey, region);
    }
}
