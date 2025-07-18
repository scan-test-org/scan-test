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
            // 第一步：用 contextPath=/ 初始化登录
            Properties loginProps = new Properties();
            loginProps.setProperty("serverAddr", serverAddr);
            loginProps.setProperty("username", username);
            loginProps.setProperty("password", password);
            loginProps.setProperty("contextPath", "/");
            Object loginService = AiMaintainerFactory.createAiMaintainerService(loginProps);

            // 尝试用反射获取 accessToken
            String accessToken = null;
            try {
                java.lang.reflect.Field field = loginService.getClass().getDeclaredField("accessToken");
                field.setAccessible(true);
                accessToken = (String) field.get(loginService);
            } catch (Exception e) {
                log.error("无法通过反射获取 accessToken，后续业务接口可能会失败", e);
            }

            // 第二步：用 contextPath=/nacos 和 accessToken 初始化业务用的 mcpMaintainerService
            Properties apiProps = new Properties();
            apiProps.setProperty("serverAddr", serverAddr);
            apiProps.setProperty("contextPath", "/nacos");
            if (accessToken != null) {
                apiProps.setProperty("accessToken", accessToken);
            }
            mcpMaintainerService = (McpMaintainerService) AiMaintainerFactory.createAiMaintainerService(apiProps);
        } catch (Exception e) {
            log.error("Failed to init McpMaintainerService", e);
        }
    }

    /**
     * 获取MCP能力市场卡片列表
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param keyword 关键字
     * @return 能力市场卡片DTO列表
     */
    @Override
    public List<McpMarketCardDto> list(int pageNo, int pageSize, String keyword) {
        try {
            Page<McpServerBasicInfo> page = mcpMaintainerService.listMcpServer(keyword, pageNo, pageSize);
            if (page == null || page.getPageItems() == null) {
                return Collections.emptyList();
            }
            return page.getPageItems().stream().map(item ->
                McpMarketCardDto.builder()
                    .id(item.getId())
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
     * @param mcpId 服务唯一ID
     * @return 能力市场详情DTO
     */
    @Override
    public McpMarketDetailDto detail(String mcpId) {
        try {
            McpServerDetailInfo detail = mcpMaintainerService.getMcpServerDetail(mcpId);
            if (detail == null) {
                return null;
            }
            return McpMarketDetailDto.builder()
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
                .namespaceId(detail.getNamespaceId())
                .build();
        } catch (Exception e) {
            log.error("Error fetching mcp market detail", e);
            return null;
        }
    }
} 