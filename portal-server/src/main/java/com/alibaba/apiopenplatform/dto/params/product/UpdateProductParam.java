package com.alibaba.apiopenplatform.dto.params.product;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;


/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateProductParam extends CreateProductParam {

    @NotBlank(message = "产品ID不能为空")
    private String productId;

    private String adminId;

    private String status;

    private String enableConsumerAuth;

    private String type;

    private String document;

    private String icon;

    private String category;
}
