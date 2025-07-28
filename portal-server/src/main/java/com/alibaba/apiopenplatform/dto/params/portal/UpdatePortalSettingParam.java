package com.alibaba.apiopenplatform.dto.params.portal;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * @author zh
 */
@Data
public class UpdatePortalSettingParam {

    private Boolean builtinAuthEnabled;

    private Boolean oidcAuthEnabled;

    private String frontendRedirectUrl;

    private Boolean autoApproveDevelopers;

    private Boolean autoApproveSubscriptions;

    @Valid
    private List<OidcOption> oidcOptions;

//    @AssertTrue(message = "OIDC 鉴权配置不能为空")
//    private boolean isOidcConfigValid() {
//        return !Boolean.TRUE.equals(oidcAuthEnabled) || oidcOption != null;
//    }
}
