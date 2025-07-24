package com.alibaba.apiopenplatform.dto.params.developer;

/**
 * OIDC 换 token 请求参数 DTO
 * 用于 /oauth/token 接口
 * @author zxd
 */
public class OidcTokenRequestDto {
    private String code;
    private String state;
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
} 