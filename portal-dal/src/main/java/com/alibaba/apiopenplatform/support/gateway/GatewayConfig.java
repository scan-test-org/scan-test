package com.alibaba.apiopenplatform.support.gateway;

import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.Builder;
import lombok.Data;

/**
 * @author zh
 */
@Data
@Builder
public class GatewayConfig {

    private GatewayType gatewayType;

    private APIGConfig apigConfig;

    private HigressConfig higressConfig;
}
