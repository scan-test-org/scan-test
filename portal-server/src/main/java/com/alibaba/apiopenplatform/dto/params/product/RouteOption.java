package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.support.product.RouteConfig;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class RouteOption implements InputConverter<RouteConfig> {

    private String routeId;

    private String name;
}
