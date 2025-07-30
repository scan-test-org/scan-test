package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.product.NacosRefConfig;

import javax.persistence.Converter;

/**
 * @author zxd
 */
@Converter
public class NacosRefConfigConverter extends JsonConverter<NacosRefConfig> {

    public NacosRefConfigConverter() {
        super(NacosRefConfig.class);
    }
} 