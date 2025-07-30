package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Product;
import com.alibaba.apiopenplatform.support.enums.ProductStatus;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class ProductResult implements OutputConverter<ProductResult, Product> {

    private String productId;

    private String name;

    private String description;

    private ProductStatus status = ProductStatus.PENDING;

    private Boolean enableConsumerAuth;

    private ProductType type;

    private String document;

    private String icon;

    private String category;

    private String apiSpec;

    private String mcpSpec;

    private Boolean enabled;
}
