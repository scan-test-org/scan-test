package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductVersion;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Create Product Version Param
 * @author zh
 */
@Data
public class CreateProductVersionParam implements InputConverter<ProductVersion> {

    private String productId;

    @NotBlank(message = "版本号不能为空")
    @Size(max = 20, message = "版本号长度不能超过20个字符")
    private String version;

    private String description;

    private String apiList;

    private String authType;

    private String rateLimit;
} 