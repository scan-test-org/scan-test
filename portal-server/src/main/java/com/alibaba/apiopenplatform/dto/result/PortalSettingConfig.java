package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class PortalSettingConfig implements OutputConverter<PortalSettingConfig, PortalSetting> {

    private Boolean builtinAuthEnabled;

    private Boolean oidcAuthEnabled;

    private OidcConfig oidcConfig;

    private Boolean autoApproveDevelopers;

    private Boolean autoApproveSubscriptions;

    private String frontendRedirectUrl;

    private List<OidcConfig> oidcConfigs;

    @Override
    public PortalSettingConfig convertFrom(PortalSetting source) {
        this.setBuiltinAuthEnabled(source.getBuiltinAuthEnabled());
        this.setOidcAuthEnabled(source.getOidcAuthEnabled());
        this.setAutoApproveDevelopers(source.getAutoApproveDevelopers());
        this.setAutoApproveSubscriptions(source.getAutoApproveSubscriptions());
        this.setFrontendRedirectUrl(source.getFrontendRedirectUrl());
        // 只返回数组
        if (source.getOidcConfigs() != null) {
            this.setOidcConfigs(source.getOidcConfigs());
        } else {
            this.setOidcConfigs(java.util.Collections.emptyList());
        }
        return this;
    }
}
