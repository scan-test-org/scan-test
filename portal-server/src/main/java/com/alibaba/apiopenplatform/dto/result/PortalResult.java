package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Portal;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@Data
public class PortalResult implements OutputConverter<PortalResult, Portal> {

    private String portalId;

    private String name;

    private String title;

    private String description;

    private String adminId;

    private PortalSettingConfig portalSettingConfig;

    private PortalUiConfig portalUiConfig;

    private List<PortalDomainConfig> portalDomainConfig;

    @Override
    public PortalResult convertFrom(Portal source) {
        OutputConverter.super.convertFrom(source);
        portalSettingConfig = new PortalSettingConfig().convertFrom(source.getPortalSetting());
        portalUiConfig = new PortalUiConfig().convertFrom(source.getPortalUi());
        portalDomainConfig = source.getPortalDomains().stream().map(domain -> new PortalDomainConfig().convertFrom(domain)).collect(Collectors.toList());
        return this;
    }
}
