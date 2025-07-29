package com.alibaba.apiopenplatform.dto.params.developer;

import lombok.Data;

/**
 * OIDC 换 token 请求参数 DTO
 * 用于 /oauth/token 接口
 * @author zxd
 */
@Data
public class OidcTokenRequestParam {
    private String code;
    private String state;
} 