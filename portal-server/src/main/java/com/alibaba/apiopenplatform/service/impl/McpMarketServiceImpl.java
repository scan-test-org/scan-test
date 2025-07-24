package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketCardParam;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketDetailParam;
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
@Service
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
    public List<McpMarketCardParam> list(int pageNo, int pageSize, String mcpName, String namespaceId) {
        try {
            Page<McpServerBasicInfo> page = mcpMaintainerService.listMcpServer(namespaceId, mcpName, pageNo, pageSize);
            if (page == null || page.getPageItems() == null) {
                return Collections.emptyList();
            }
            return page.getPageItems().stream().map(item -> {
                McpMarketCardParam param = new McpMarketCardParam();
                param.setId(item.getId());
                param.setName(item.getName());
                param.setProtocol(item.getProtocol());
                param.setFrontProtocol(item.getFrontProtocol());
                param.setDescription(item.getDescription());
                param.setRepository(item.getRepository());
                param.setVersionDetail(item.getVersionDetail());
                param.setVersion(item.getVersion());
                param.setRemoteServerConfig(item.getRemoteServerConfig());
                param.setLocalServerConfig(item.getLocalServerConfig());
                param.setEnabled(item.isEnabled());
                param.setCapabilities(item.getCapabilities());
                param.setMcpName(item.getName()); // mcpName用name
                // param.setNamespaceId(item.getNamespace()); // 无此方法，跳过
                return param;
            }).collect(Collectors.toList());
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
    public McpMarketDetailParam detail(String mcpName, String namespaceId, String version) {
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
            McpMarketDetailParam param = new McpMarketDetailParam();
            param.setId(detail.getId());
            param.setName(detail.getName());
            param.setProtocol(detail.getProtocol());
            param.setFrontProtocol(detail.getFrontProtocol());
            param.setDescription(detail.getDescription());
            param.setRepository(detail.getRepository());
            param.setVersionDetail(detail.getVersionDetail());
            param.setVersion(detail.getVersion());
            param.setRemoteServerConfig(detail.getRemoteServerConfig());
            param.setLocalServerConfig(detail.getLocalServerConfig());
            param.setEnabled(detail.isEnabled());
            param.setCapabilities(detail.getCapabilities());
            param.setBackendEndpoints(detail.getBackendEndpoints());
            param.setToolSpec(detail.getToolSpec());
            param.setAllVersions(detail.getAllVersions());
            param.setNamespaceId(null); // 无getNamespace方法，直接赋null
            param.setMcpName(detail.getName()); // mcpName用name
            return param;
        } catch (Exception e) {
            log.error("获取MCP Server详情时发生异常，namespaceId: {}, mcpName: {}, version: {}", namespaceId, mcpName, version, e);
            return null;
        }
    }
} 