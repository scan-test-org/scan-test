package com.alibaba.apiopenplatform.dto.params.product;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ProductRef;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import com.alibaba.apiopenplatform.support.product.APIGRefConfig;
import com.alibaba.apiopenplatform.support.product.HigressRefConfig;
import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * @author zh
 */
@Data
public class CreateProductRefParam implements InputConverter<ProductRef> {

    @NotNull(message = "数据源类型不能为空")
    private SourceType sourceType;

    private String gatewayId;

    private String nacosId;

    private APIGRefConfig apigRefConfig;

    private HigressRefConfig higressRefConfig;

    private NacosRefConfig nacosRefConfig;

}