package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ProductRef;
import lombok.Data;

/**
 * API Reference Result
 * @author zh
 */
@Data
public class ProductRefResult implements OutputConverter<ProductRefResult, ProductRef> {

    private Long id;
    private String apiId;
    private String gatewayId;
    private String productId;

    @Override
    public ProductRefResult convertFrom(ProductRef source) {
        OutputConverter.super.convertFrom(source);
        return this;
    }
} 