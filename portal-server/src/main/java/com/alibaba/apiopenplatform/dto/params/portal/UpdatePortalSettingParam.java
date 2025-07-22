package com.alibaba.apiopenplatform.dto.params.portal;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author zh
 */
@Data
public class UpdatePortalSettingParam {

    @NotBlank(message = "门户ID不能为空")
    private String portalId;

    private Boolean builtinAuthEnabled;

    private Boolean oidcAuthEnabled;

    private String frontendRedirectUrl;

    // 移除单个oidcConfigParam字段及相关校验

    private Boolean autoApproveDevelopers;

    private Boolean autoApproveSubscriptions;

    private List<OidcConfigParam> oidcConfigParams;

    @AssertTrue(message = "OIDC 鉴权配置不能为空")
    private boolean isOidcConfigValid() {
        return !Boolean.TRUE.equals(oidcAuthEnabled) || oidcConfigParams != null && !oidcConfigParams.isEmpty();
    }
}
