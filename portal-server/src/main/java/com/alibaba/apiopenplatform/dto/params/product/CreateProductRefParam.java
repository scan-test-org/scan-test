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

/**
 * @author zh
 */
@Data
public class CreateProductRefParam implements InputConverter<ProductRef> {

    private String apiId;

    @NotBlank(message = "网关ID不能为空")
    private String gatewayId;

    private List<String> routes;

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
} 