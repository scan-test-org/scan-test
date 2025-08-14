package com.alibaba.apiopenplatform.dto.params.gateway;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.gateway.APIGConfig;
import com.alibaba.apiopenplatform.support.gateway.HigressConfig;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zh
 */
@Data
public class ImportGatewayParam implements InputConverter<Gateway> {

    @NotBlank(message = "网关名称不能为空")
    private String gatewayName;

    private String description;

    @NotNull(message = "网关类型不能为空")
    private GatewayType gatewayType;

    private String gatewayId;

    private APIGConfig apigConfig;

    private HigressConfig higressConfig;

    @AssertTrue(message = "网关配置无效")
    private boolean isGatewayConfigValid() {
        return gatewayType.isAPIG() && apigConfig != null && StrUtil.isNotBlank(gatewayId)
                || gatewayType.isHigress() && higressConfig != null;
    }
}
