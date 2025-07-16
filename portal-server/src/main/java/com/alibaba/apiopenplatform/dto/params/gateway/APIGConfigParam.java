package com.alibaba.apiopenplatform.dto.params.gateway;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.support.gateway.APIGConfig;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class APIGConfigParam implements InputConverter<APIGConfig> {

    @NotBlank(message = "accessKey不能为空")
    private String accessKey;

    @NotBlank(message = "secretKey不能为空")
    private String secretKey;

    @NotBlank(message = "网关region不能为空")
    private String region;

    @NotBlank(message = "网关ID不能为空")
    private String gatewayId;
}
