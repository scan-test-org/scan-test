package com.alibaba.apiopenplatform.support.gateway;

import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.Builder;
import lombok.Data;

/**
 * @author zh
 */
@Data
@Builder
public class GatewayIdentityConfig {

    private GatewayType gatewayType;

    private APIGConfig apigConfig;

    private HigressConfig higressConfig;

    public String getIdentity() {
        return gatewayType.isHigress() ? JSONUtil.toJsonStr(higressConfig) : JSONUtil.toJsonStr(apigConfig);
    }
}
