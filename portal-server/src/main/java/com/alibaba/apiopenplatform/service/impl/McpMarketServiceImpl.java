package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.dto.mcp.McpMarketCardDto;
import com.alibaba.apiopenplatform.dto.mcp.McpMarketDetailDto;
import com.alibaba.apiopenplatform.service.McpMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.alibaba.nacos.maintainer.client.naming.NamingMaintainerService;
import com.alibaba.nacos.maintainer.client.naming.NamingMaintainerFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.McpMaintainerService;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.model.Page;

/**
 * MCP能力市场聚合服务实现
 * 基于Nacos Maintainer SDK聚合Nacos注册的MCP Server能力，供前端统一浏览、查询、详情展示。
 * 字段与Nacos官方文档和model保持一致。
 * @author zxd
 */
//@Service
@RequiredArgsConstructor
@Slf4j
public class McpMarketServiceImpl implements McpMarketService {

    @Value("${nacos.maintainer.server-addr}")
    private String serverAddr;
    @Value("${nacos.maintainer.username}")
    private String username;
    @Value("${nacos.maintainer.password}")
    private String password;

    private McpMaintainerService mcpMaintainerService;

    @PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            // serverAddr 只写主机和端口，不带 /nacos 前缀
            properties.setProperty("serverAddr", serverAddr);
            properties.setProperty("username", username);
            properties.setProperty("password", password);
            // 强制指定 contextPath 为 /，防止 SDK 自动加 /nacos
            properties.setProperty("contextPath", "nacos");
            mcpMaintainerService = (McpMaintainerService) AiMaintainerFactory.createAiMaintainerService(properties);
        } catch (Exception e) {
            log.error("Failed to init McpMaintainerService", e);
        }
    }

    /**
     * 获取MCP能力市场卡片列表，支持namespaceId
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param mcpName 关键字
     * @param namespaceId 命名空间ID
     * @return 能力市场卡片DTO列表
     */
    public List<McpMarketCardDto> list(int pageNo, int pageSize, String mcpName, String namespaceId) {
        try {
            Page<McpServerBasicInfo> page = mcpMaintainerService.listMcpServer(namespaceId, mcpName, pageNo, pageSize);
            if (page == null || page.getPageItems() == null) {
                return Collections.emptyList();
            }
            return page.getPageItems().stream().map(item ->
                McpMarketCardDto.builder()
                    .id(item.getId())
                    .mcpName(item.getName()) // 新增字段
                    .name(item.getName())
                    .protocol(item.getProtocol())
                    .frontProtocol(item.getFrontProtocol())
                    .description(item.getDescription())
                    .repository(item.getRepository())
                    .versionDetail(item.getVersionDetail())
                    .version(item.getVersion())
                    .remoteServerConfig(item.getRemoteServerConfig())
                    .localServerConfig(item.getLocalServerConfig())
                    .enabled(item.isEnabled())
                    .capabilities(item.getCapabilities())
                    .build()
            ).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching mcp market list", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取MCP能力服务详情
     * @param mcpName 服务名（不是id）
     * @param namespaceId 命名空间ID
     * @param version 版本
     * @return 能力市场详情DTO
     */
    @Override
    public McpMarketDetailDto detail(String mcpName, String namespaceId, String version) {
        log.info("开始获取MCP Server详情，namespaceId: {}, mcpName: {}, version: {}", namespaceId, mcpName, version);
        try {
            if (mcpMaintainerService == null) {
                log.error("mcpMaintainerService 未初始化");
                return null;
            }
            McpServerDetailInfo detail = mcpMaintainerService.getMcpServerDetail(namespaceId, mcpName, version);
            if (detail == null) {
                log.warn("getMcpServerDetail 返回 null，namespaceId: {}, mcpName: {}, version: {}", namespaceId, mcpName, version);
                return null;
            }
            log.info("成功获取到详情数据，id: {}, name: {}", detail.getId(), detail.getName());
            return McpMarketDetailDto.builder()
                .id(detail.getId())
                .mcpName(detail.getName())
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
                .namespaceId(detail.getNamespaceId())
                .build();
        } catch (Exception e) {
            log.error("获取MCP Server详情时发生异常，namespaceId: {}, mcpName: {}, version: {}", namespaceId, mcpName, version, e);
            return null;
        }
    }
}