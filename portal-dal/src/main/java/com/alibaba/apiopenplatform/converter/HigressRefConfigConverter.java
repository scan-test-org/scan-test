package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.product.HigressRefConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class HigressRefConfigConverter extends JsonConverter<HigressRefConfig> {

    protected HigressRefConfigConverter() {
        super(HigressRefConfig.class);
    }
}
