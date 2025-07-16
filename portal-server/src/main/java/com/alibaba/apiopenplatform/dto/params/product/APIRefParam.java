package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.APIRef;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * API Reference Parameter
 * @author zh
 */
@Data
public class APIRefParam implements InputConverter<APIRef> {

    @NotBlank(message = "API ID不能为空")
    private String apiId;

    @NotBlank(message = "网关ID不能为空")
    private String gatewayId;

    private String productId;
} 