package com.alibaba.apiopenplatform.dto.params.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductRef;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
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

    @NotNull(message = "数据源类型不能为空")
    private SourceType sourceType;

    @AssertTrue(message = "数据源配置无效")
    private boolean isSourceValid() {
        if (sourceType == SourceType.GATEWAY) {
            return StrUtil.isNotBlank(gatewayId);
        } else if (sourceType == SourceType.NACOS) {
            return StrUtil.isNotBlank(nacosId);
        }
        return false;
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