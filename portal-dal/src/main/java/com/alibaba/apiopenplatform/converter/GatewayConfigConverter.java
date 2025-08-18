package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.gateway.GatewayConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class GatewayConfigConverter extends JsonConverter<GatewayConfig> {

    public GatewayConfigConverter() {
        super(GatewayConfig.class);
    }
}
