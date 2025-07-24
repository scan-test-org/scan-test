package com.alibaba.apiopenplatform.dto.params.portal;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import java.util.List;

/**
 * @author zh
 */
@Data
public class UpdatePortalSettingParam {

    private Boolean builtinAuthEnabled;

    private Boolean oidcAuthEnabled;

    @Valid
    private OidcOption oidcOption;
    private String frontendRedirectUrl;

    // 移除单个oidcConfigParam字段及相关校验

    private Boolean autoApproveDevelopers;

    private Boolean autoApproveSubscriptions;

    private List<OidcOption> oidcOptions;

    @AssertTrue(message = "OIDC 鉴权配置不能为空")
    private boolean isOidcConfigValid() {
        return !Boolean.TRUE.equals(oidcAuthEnabled) || oidcOption != null;
    }
}
