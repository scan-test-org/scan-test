package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.support.enums.ProductType;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class QueryProductParam {

    private String portalId;

    private ProductType type;

    private String name;

    private String category;
}
