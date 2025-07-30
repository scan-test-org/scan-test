package com.alibaba.apiopenplatform.support.portal;

import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class PortalSettingConfig {

    private Boolean builtinAuthEnabled = true;

    private Boolean oidcAuthEnabled = false;

    private List<OidcConfig> oidcConfigs;

    private Boolean autoApproveDevelopers = false;

    private Boolean autoApproveSubscriptions = false;

    private String frontendRedirectUrl;
}
