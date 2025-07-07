package com.alibaba.apiopenplatform.dto.params.developer;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 绑定第三方账号参数
 * @author zxd
 */
@Data
public class DeveloperBindExternalDto {
    @NotBlank(message = "providerName不能为空")
    private String providerName;

    @NotBlank(message = "providerSubject不能为空")
    private String providerSubject;

    private String displayName;
    private String rawInfoJson;
} 