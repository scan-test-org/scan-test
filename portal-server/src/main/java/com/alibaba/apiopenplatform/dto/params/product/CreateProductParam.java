package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Product;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


/**
 * @author zh
 */
@Data
public class CreateProductParam implements InputConverter<Product> {

    @NotBlank(message = "产品名称不能为空")
    @Size(max = 50, message = "产品名称长度不能超过50个字符")
    private String name;

    private String description;

    @NotBlank(message = "管理员ID不能为空")
    private String ownerId;

    private String adminId;

    private String status;

    private String enableConsumerAuth;

    private String type;

    private String document;

    private String icon;

    private String category;
}
