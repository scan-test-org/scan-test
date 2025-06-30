package com.alibaba.apiopenplatform.dto.params.portal;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.support.OidcConfig;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class OidcConfigParam implements InputConverter<OidcConfig> {

    @NotBlank(message = "OIDC client ID不能为空")
    private String clientId;

    @NotBlank(message = "OIDC client secret不能为空")
    private String clientSecret;

    @NotBlank(message = "OIDC issuer不能为空")
    private String issuer;

    private String scopes;
}
