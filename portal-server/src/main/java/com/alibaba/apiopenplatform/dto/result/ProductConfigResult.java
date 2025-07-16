package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ProductConfig;
import lombok.Data;

/**
 * Product Config Result
 * @author zh
 */
@Data
public class ProductConfigResult implements OutputConverter<ProductConfigResult, ProductConfig> {

    private String configId;
    private String productId;
    private String configKey;
    private String configValue;
    private String description;

    @Override
    public ProductConfigResult convertFrom(ProductConfig source) {
        OutputConverter.super.convertFrom(source);
        return this;
    }
} 