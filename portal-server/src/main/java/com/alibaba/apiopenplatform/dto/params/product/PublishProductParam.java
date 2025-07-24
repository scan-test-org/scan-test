package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductPublication;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class PublishProductParam implements InputConverter<ProductPublication> {

    @NotBlank(message = "门户ID不能为空")
    private String portalId;
}
