package com.alibaba.apiopenplatform.support.converter;

import com.alibaba.apiopenplatform.support.product.RouteConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class RouteConfigConverter extends JsonConverter<RouteConfig> {

    protected RouteConfigConverter() {
        super(RouteConfig.class);
    }
}
