package com.alibaba.apiopenplatform.dto.params.mcp;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * MCP 市场详情参数
 * @author zxd
 */
@Data
@Builder
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
    
    /**
     * 从 McpServerDetailInfo 转换
     */
    public static McpMarketDetailParam from(com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo detail) {
        return McpMarketDetailParam.builder()
                .id(detail.getId())
                .name(detail.getName())
                .protocol(detail.getProtocol())
                .frontProtocol(detail.getFrontProtocol())
                .description(detail.getDescription())
                .repository(detail.getRepository())
                .versionDetail(detail.getVersionDetail())
                .version(detail.getVersion())
                .remoteServerConfig(detail.getRemoteServerConfig())
                .localServerConfig(detail.getLocalServerConfig())
                .enabled(detail.isEnabled())
                .capabilities(detail.getCapabilities())
                .backendEndpoints(detail.getBackendEndpoints())
                .toolSpec(detail.getToolSpec())
                .allVersions(detail.getAllVersions())
                .namespaceId(null)
                .mcpName(detail.getName())
                .build();
    }
} 