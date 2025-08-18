package com.alibaba.apiopenplatform.support.product;

import lombok.Data;


/**
 * @author zh
 */
@Data
public class APIGRefConfig {

    private String apiId;

    private String apiName;

    /**
     * MCP标识信息
     */
    private String mcpRouteId;
    private String mcpServerName;


}
