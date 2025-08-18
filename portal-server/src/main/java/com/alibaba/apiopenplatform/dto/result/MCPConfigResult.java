package com.alibaba.apiopenplatform.dto.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class MCPConfigResult {

    protected String mcpServerName;

    protected MCPServerConfig mcpServerConfig;

    protected String tools;

    protected McpMetadata meta;

    @Data
    public static class McpMetadata {

        /**
         * 来源
         * AI网关/Higress/Nacos
         */
        private String source;

        /**
         * 服务类型
         * AI网关：HTTP（HTTP转MCP）/MCP（MCP直接代理）
         * Higress：OPEN_API（OpenAPI转MCP）/DIRECT_ROUTE（直接路由）/DATABASE（数据库）
         */
        private String fromType;

    }

    @Data
    public static class MCPServerConfig {
        /**
         * for gateway
         */
        private String path;
        private List<Domain> domains;

        /**
         * for nacos
         */
        private Object rawConfig;
    }

    @Data
    @Builder
    public static class Domain {
        private String domain;
        private String protocol;
    }
}
