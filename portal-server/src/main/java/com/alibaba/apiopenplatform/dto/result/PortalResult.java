package com.alibaba.apiopenplatform.dto.result;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.entity.PortalDomain;
import com.alibaba.apiopenplatform.support.enums.DomainType;
import com.alibaba.apiopenplatform.support.enums.ProtocolType;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import com.alibaba.apiopenplatform.support.portal.PortalUiConfig;
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

    private String description;

    private String adminId;

    private PortalSettingConfig portalSettingConfig;

    private PortalUiConfig portalUiConfig;

    private List<PortalDomainConfig> portalDomainConfig;

    @Override
    public PortalResult convertFrom(Portal source) {
        OutputConverter.super.convertFrom(source);
        if (CollUtil.isNotEmpty(source.getPortalDomains())) {
            portalDomainConfig = source.getPortalDomains().stream().map(domain -> new PortalDomainConfig().convertFrom(domain)).collect(Collectors.toList());
        }
        return this;
    }

    @Data
    static
    class PortalDomainConfig implements OutputConverter<PortalDomainConfig, PortalDomain> {

        private String domain;

        private DomainType type;

        private ProtocolType protocol = ProtocolType.HTTP;
    }
}
