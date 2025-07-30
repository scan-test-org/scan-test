package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;

import javax.persistence.Converter;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class PortalSettingConfigConverter extends JsonConverter<PortalSettingConfig> {

    public PortalSettingConfigConverter() {
        super(PortalSettingConfig.class);
    }
}
