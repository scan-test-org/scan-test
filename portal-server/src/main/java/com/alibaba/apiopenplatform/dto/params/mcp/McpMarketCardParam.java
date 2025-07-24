package com.alibaba.apiopenplatform.dto.params.mcp;

import lombok.Data;
import java.util.List;

/**
 * MCP 市场卡片参数
 * @author zxd
 */
@Data
public class McpMarketCardParam {
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
    private String mcpName;
} 