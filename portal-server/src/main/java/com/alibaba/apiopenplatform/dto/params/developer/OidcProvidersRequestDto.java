package com.alibaba.apiopenplatform.dto.params.developer;

/**
 * OIDC provider 查询请求参数 DTO
 * 用于 /oauth/providers 接口
 * @author zxd
 */
public class OidcProvidersRequestDto {
    private String portalId;
    public String getPortalId() { return portalId; }
    public void setPortalId(String portalId) { this.portalId = portalId; }
} 