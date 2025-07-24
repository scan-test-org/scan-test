package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Product;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;


/**
 * @author zh
 */
@Data
public class UpdateProductParam implements InputConverter<Product> {

    private String name;

    private String description;

    private ProductType type;

    private Boolean enableConsumerAuth;

    private String document;

    private String icon;

    private String category;
}
