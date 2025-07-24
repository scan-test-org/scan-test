package com.alibaba.apiopenplatform.dto.params.gateway;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
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

    @NotBlank(message = "网关ID不能为空")
    private String gatewayId;

    @Valid
    private APIGOption apigOption;

    @Valid
    private HigressOption higressOption;

    @AssertTrue(message = "网关配置不能为空")
    private boolean isGatewayConfigValid() {
        return gatewayType.isAPIG() && apigOption != null || gatewayType.isHigress() && higressOption != null;
    }

    @Override
    public Gateway convertTo() {
        Gateway gateway = InputConverter.super.convertTo();
        if (higressOption != null) {
            gateway.setHigressConfig(higressOption.convertTo());
        }
        if (apigOption != null) {
            gateway.setApigConfig(apigOption.convertTo());
        }
        return gateway;
    }
}
