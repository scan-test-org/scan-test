package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.consumer.ConsumerAuthConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class ConsumerAuthConfigConverter extends JsonConverter<ConsumerAuthConfig> {

    public ConsumerAuthConfigConverter() {
        super(ConsumerAuthConfig.class);
    }
}
