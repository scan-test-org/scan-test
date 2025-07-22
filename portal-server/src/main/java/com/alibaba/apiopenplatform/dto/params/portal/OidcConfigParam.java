package com.alibaba.apiopenplatform.dto.params.portal;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zh
 */
@Data
public class OidcConfigParam implements InputConverter<OidcConfig> {

    private String id;
    @NotBlank(message = "provider不能为空")
    private String provider;

    @NotBlank(message = "name不能为空")
    private String name;

    private String logoUrl;

    @NotBlank(message = "clientId不能为空")
    private String clientId;

    @NotBlank(message = "clientSecret不能为空")
    private String clientSecret;

    private String scopes;

    @NotBlank(message = "authorizationEndpoint不能为空")
    private String authorizationEndpoint;

    @NotBlank(message = "tokenEndpoint不能为空")
    private String tokenEndpoint;

    @NotBlank(message = "userInfoEndpoint不能为空")
    private String userInfoEndpoint;

    private String jwkSetUri;

    @NotBlank(message = "redirectUri不能为空")
    private String redirectUri;

    @NotNull(message = "enabled不能为空")
    private Boolean enabled;
}
