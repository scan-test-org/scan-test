package com.alibaba.apiopenplatform.support.converter;

import com.alibaba.apiopenplatform.support.gateway.APIGConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class APIGConfigConverter extends JsonConverter<APIGConfig> {

    protected APIGConfigConverter() {
        super(APIGConfig.class);
    }
}
