package com.alibaba.apiopenplatform.dto.mcp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * MCP Market 卡片DTO
 * 展示 MCP Server 的关键信息
 * @author zxd
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McpMarketCardDto {
    private String id; // MCP服务唯一ID
    private String name; // 服务名
    private String protocol; // 协议类型
    private String frontProtocol; // 前端暴露协议
    private String description; // 服务描述
    private Object repository; // 服务仓库（类型与 nacos model 保持一致）
    private Object versionDetail; // 当前最新版本信息
    private String version; // 版本号
    private Object remoteServerConfig; // 远端服务配置信息
    private Object localServerConfig; // 本地服务配置信息
    private boolean enabled; // 是否启用
    private java.util.List<?> capabilities; // 能力类型
    private String mcpName; // MCP服务名（与Nacos注册时的服务名一致）
} 