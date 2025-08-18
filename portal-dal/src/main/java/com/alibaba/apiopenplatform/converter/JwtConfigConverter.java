package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.consumer.JwtConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class JwtConfigConverter extends JsonConverter<JwtConfig> {

    protected JwtConfigConverter() {
        super(JwtConfig.class);
    }
}
