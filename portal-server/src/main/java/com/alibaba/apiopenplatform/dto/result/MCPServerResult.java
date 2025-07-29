package com.alibaba.apiopenplatform.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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

    protected String name;

    @Schema(hidden = true)
    protected String fromType;

    @Schema(hidden = true)
    protected String fromGateway;

    @Schema(hidden = true)
    protected List<Domain> domains;

    @Schema(hidden = true)
    protected String mcpServerConfig;

    @Data
    @Builder
    protected static class Domain {
        private String domain;
        private String protocol;
    }
}