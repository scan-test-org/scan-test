package com.alibaba.apiopenplatform.support.converter;

import com.alibaba.apiopenplatform.support.portal.OidcConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class OidcConfigConverter extends JsonConverter<OidcConfig> {

    public OidcConfigConverter() {
        super(OidcConfig.class);
    }
}
