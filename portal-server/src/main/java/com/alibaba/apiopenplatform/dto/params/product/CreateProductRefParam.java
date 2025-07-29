package com.alibaba.apiopenplatform.dto.params.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductRef;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@Data
public class CreateProductRefParam implements InputConverter<ProductRef> {

    private String apiId;

    private String gatewayId;

    private String nacosId;

    private List<RouteOption> routes;

    @NotNull(message = "API产品类型不能为空")
    private ProductType type;

    @AssertTrue(message = "MCP Server路由ID不能为空")
    private boolean isMCPValid() {
        if (type == ProductType.MCP_SERVER) {
            return CollUtil.isNotEmpty(routes);
        } else {
            return StrUtil.isNotBlank(apiId);
        }
    }

    @AssertTrue(message = "Gateway ID或Nacos ID必须指定一个")
    private boolean isSourceValid() {
        if (type == ProductType.MCP_SERVER) {
            return StrUtil.isNotBlank(nacosId);
        } else {
            return StrUtil.isNotBlank(gatewayId);
        }
    }

    @Override
    public ProductRef convertTo() {
        ProductRef productRef = InputConverter.super.convertTo();
        if (routes != null) {
            productRef.setRoutes(routes.stream().map(RouteOption::convertTo).collect(Collectors.toList()));
        }
        return productRef;
    }
}