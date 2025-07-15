package com.alibaba.apiopenplatform.support.gateway;

import lombok.Data;

/**
 * @author zh
 */
@Data
public class APIGConfig {

    private String accessKey;

    private String secretKey;

    private String region;

    private String gatewayId;
}
