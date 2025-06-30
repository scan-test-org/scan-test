package com.alibaba.apiopenplatform.dto.params.portal;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class UpdatePortalSettingParam {

    @NotBlank(message = "门户ID不能为空")
    private String portalId;

    private Boolean builtinAuthEnabled;

    private Boolean oidcAuthEnabled;

    @Valid
    private OidcConfigParam oidcConfigParam;

    private Boolean autoApproveDevelopers;

    private Boolean autoApproveSubscriptions;

    @AssertTrue(message = "OIDC 鉴权配置不能为空")
    private boolean isOidcConfigValid() {
        return !Boolean.TRUE.equals(oidcAuthEnabled) || oidcConfigParam != null;
    }
}
