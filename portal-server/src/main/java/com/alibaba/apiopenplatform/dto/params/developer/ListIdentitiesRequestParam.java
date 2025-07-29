package com.alibaba.apiopenplatform.dto.params.developer;

import lombok.Data;

/**
 * 外部身份列表查询请求参数 DTO
 * 用于 /oauth/list-identities 接口
 * @author zxd
 */
@Data
public class ListIdentitiesRequestParam {
    private String userId;
} 