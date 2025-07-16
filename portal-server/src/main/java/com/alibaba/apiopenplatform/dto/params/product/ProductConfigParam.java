package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductConfig;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Product Config Param
 * @author zh
 */
@Data
public class ProductConfigParam implements InputConverter<ProductConfig> {

    private String productId;

    @NotBlank(message = "配置键不能为空")
    @Size(max = 50, message = "配置键长度不能超过50个字符")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    private String description;
} 