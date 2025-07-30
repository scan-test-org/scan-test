package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ProductRef;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import com.alibaba.apiopenplatform.support.product.APIGRefConfig;
import com.alibaba.apiopenplatform.support.product.HigressRefConfig;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class ProductRefResult implements OutputConverter<ProductRefResult, ProductRef> {

    private String productId;

    private SourceType sourceType;

    private String gatewayId;

    private APIGRefConfig apigRefConfig;

    private HigressRefConfig higressRefConfig;

    private String nacosId;
}