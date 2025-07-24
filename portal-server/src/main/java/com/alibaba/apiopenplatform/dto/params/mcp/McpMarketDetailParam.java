package com.alibaba.apiopenplatform.dto.params.mcp;

import lombok.Data;
import java.util.List;

/**
 * MCP 市场详情参数
 * @author zxd
 */
@Data
public class McpMarketDetailParam {
    private String id;
    private String name;
    private String protocol;
    private String frontProtocol;
    private String description;
    private Object repository;
    private Object versionDetail;
    private String version;
    private Object remoteServerConfig;
    private Object localServerConfig;
    private boolean enabled;
    private List<?> capabilities;
    private List<?> backendEndpoints;
    private Object toolSpec;
    private List<?> allVersions;
    private String namespaceId;
    private String mcpName;
} 