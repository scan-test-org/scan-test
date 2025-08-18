package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.product.APIGRefConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class APIGRefConfigConverter extends JsonConverter<APIGRefConfig> {

    protected APIGRefConfigConverter() {
        super(APIGRefConfig.class);
    }
}
