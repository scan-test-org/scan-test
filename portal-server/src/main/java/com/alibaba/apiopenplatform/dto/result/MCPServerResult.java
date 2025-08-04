package com.alibaba.apiopenplatform.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zh
 */
@Data
@Schema(
        oneOf = {
                APIGMCPServerResult.class,
                HigressMCPServerResult.class,
                NacosMCPServerResult.class
        },
        discriminatorProperty = "type"
)

public class MCPServerResult {
    protected String mcpServerName;
}