package com.alibaba.apiopenplatform.dto.params.consumer;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductSubscription;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class CreateSubscriptionParam implements InputConverter<ProductSubscription> {

    @NotBlank(message = "Product ID不能为空")
    private String productId;
}