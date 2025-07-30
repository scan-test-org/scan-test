package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.gateway.HigressConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class HigressConfigConverter extends JsonConverter<HigressConfig> {

    protected HigressConfigConverter() {
        super(HigressConfig.class);
    }
}
