package com.alibaba.apiopenplatform.dto.params.gateway;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.gateway.APIGConfig;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zh
 */
@Data
public class QueryAPIGParam implements InputConverter<APIGConfig> {

    @NotBlank(message = "网关region不能为空")
    private String region;

    @NotNull(message = "网关类型不能为空")
    private GatewayType gatewayType;

    @NotBlank(message = "accessKey不能为空")
    private String accessKey;

    @NotBlank(message = "secretKey不能为空")
    private String secretKey;
}
