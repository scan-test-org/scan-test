package com.alibaba.apiopenplatform.dto.params.product;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class UpdateProductSettingParam {

    @NotBlank(message = "产品ID不能为空")
    private String productId;

    private String apiList;

    private String authType;

    private String rateLimit;
}
