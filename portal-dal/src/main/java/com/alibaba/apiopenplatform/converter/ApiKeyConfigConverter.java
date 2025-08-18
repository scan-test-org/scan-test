package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.consumer.ApiKeyConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class ApiKeyConfigConverter extends JsonConverter<ApiKeyConfig> {

    protected ApiKeyConfigConverter() {
        super(ApiKeyConfig.class);
    }
}
