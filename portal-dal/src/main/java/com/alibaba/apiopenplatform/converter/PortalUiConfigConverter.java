package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.portal.PortalUiConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class PortalUiConfigConverter extends JsonConverter<PortalUiConfig> {

    public PortalUiConfigConverter() {
        super(PortalUiConfig.class);
    }
}
