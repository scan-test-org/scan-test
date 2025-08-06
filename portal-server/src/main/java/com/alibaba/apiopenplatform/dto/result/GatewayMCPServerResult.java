package com.alibaba.apiopenplatform.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 网关 MCP Server 结果基类
 * @author zh
 */
@Data
@Schema(
        oneOf = {
                APIGMCPServerResult.class,
                HigressMCPServerResult.class
        },
        discriminatorProperty = "type"
)
public class GatewayMCPServerResult {
    protected String mcpServerName;
} 