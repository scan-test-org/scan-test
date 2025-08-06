package com.alibaba.apiopenplatform.dto.params.mcp;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * MCP 市场详情参数
 * @author zxd
 */
@Data
@Builder
public class McpMarketDetailParam implements OutputConverter<McpMarketDetailParam, McpServerDetailInfo> {
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