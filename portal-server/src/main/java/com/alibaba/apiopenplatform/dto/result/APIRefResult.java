package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.APIRef;
import lombok.Data;

/**
 * API Reference Result
 * @author zh
 */
@Data
public class APIRefResult implements OutputConverter<APIRefResult, APIRef> {

    private Long id;
    private String apiId;
    private String gatewayId;
    private String productId;

    @Override
    public APIRefResult convertFrom(APIRef source) {
        OutputConverter.super.convertFrom(source);
        return this;
    }
} 