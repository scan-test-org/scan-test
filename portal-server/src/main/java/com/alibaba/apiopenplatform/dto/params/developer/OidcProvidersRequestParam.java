package com.alibaba.apiopenplatform.dto.params.developer;

import lombok.Data;

/**
 * OIDC provider 查询请求参数 DTO
 * 用于 /oauth/providers 接口
 * @author zxd
 */
@Data
public class OidcProvidersRequestParam {
    private String portalId;
} 