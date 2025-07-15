package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import lombok.Data;

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
}
