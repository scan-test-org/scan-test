package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.consumer.HmacConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class HmacConfigConverter extends JsonConverter<HmacConfig> {

    protected HmacConfigConverter() {
        super(HmacConfig.class);
    }
}
