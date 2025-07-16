package com.alibaba.apiopenplatform.support.portal;

import lombok.Data;

/**
 * 通用三方登录/自定义OIDC配置实体
 * 支持所有三方登录方式统一配置
 * @author zh
 */
@Data
public class OidcConfig {
    private String id; // 唯一标识
    private String provider; // 平台标识，如 github、google、aliyun、oidc
    private String name; // 显示名，如“GitHub登录”、“公司SSO”
    private String logoUrl; // 登录按钮logo
    private String clientId;
    private String clientSecret;
    private String scopes;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private String jwkSetUri;
    private String redirectUri;
    private boolean enabled; // 是否启用
}
