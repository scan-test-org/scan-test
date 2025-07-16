package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ProductVersion;
import lombok.Data;

/**
 * Product Version Result
 * @author zh
 */
@Data
public class ProductVersionResult implements OutputConverter<ProductVersionResult, ProductVersion> {

    private String versionId;
    private String productId;
    private String version;
    private String description;
    private String status;
    private String apiList;
    private String authType;
    private String rateLimit;

    @Override
    public ProductVersionResult convertFrom(ProductVersion source) {
        OutputConverter.super.convertFrom(source);
        return this;
    }
} 