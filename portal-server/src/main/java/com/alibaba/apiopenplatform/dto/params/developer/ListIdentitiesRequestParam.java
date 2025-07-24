package com.alibaba.apiopenplatform.dto.params.developer;

/**
 * 外部身份列表查询请求参数 DTO
 * 用于 /oauth/list-identities 接口
 * @author zxd
 */
public class ListIdentitiesRequestParam {
    private String userId;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
} 