package com.alibaba.apiopenplatform.dto.params.product;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductRef;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@Data
public class CreateProductRefParam implements InputConverter<ProductRef> {

    @NotBlank(message = "API产品ID不能为空")
    private String productId;

    private String apiId;

    @NotBlank(message = "网关ID不能为空")
    private String gatewayId;

    private List<RouteOption> routes;

    @Override
    public ProductRef convertTo() {
        ProductRef productRef = InputConverter.super.convertTo();
        if (CollUtil.isNotEmpty(routes)) {
            productRef.setRoutes(routes.stream().map(RouteOption::convertTo).collect(Collectors.toList()));
        }
        return productRef;
    }
}