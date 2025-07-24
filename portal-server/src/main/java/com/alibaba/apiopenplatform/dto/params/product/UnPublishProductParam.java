package com.alibaba.apiopenplatform.dto.params.product;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class UnPublishProductParam {

    @NotBlank(message = "产品ID不能为空")
    private String productId;

    @NotBlank(message = "门户ID不能为空")
    private String portalId;
}
