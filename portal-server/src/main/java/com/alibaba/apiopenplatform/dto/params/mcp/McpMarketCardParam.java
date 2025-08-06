package com.alibaba.apiopenplatform.dto.params.mcp;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import lombok.Data;
import java.util.List;

/**
 * MCP 市场卡片参数
 * @author zxd
 */
@Data
public class McpMarketCardParam implements OutputConverter<McpMarketCardParam, McpServerBasicInfo> {
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