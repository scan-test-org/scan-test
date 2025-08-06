package com.alibaba.apiopenplatform.dto.params.developer;

import lombok.Data;

/**
 * 解绑外部身份参数
 *
 * @author zxd
 */
@Data
public class UnbindExternalIdentityParam {

    private String providerName;

    private String providerSubject;

    private String portalId;
} 