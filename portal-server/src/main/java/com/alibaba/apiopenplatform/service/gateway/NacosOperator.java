package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.dto.result.NacosMCPServerResult;
import com.alibaba.apiopenplatform.entity.NacosInstance;
import com.alibaba.apiopenplatform.support.enums.NacosStatus;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.McpMaintainerService;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.model.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Nacos操作类
 * @author zxd
 */
@Component
@Slf4j
public class NacosOperator {

    /**
     * 测试Nacos连接
     */
    public boolean testConnection(NacosInstance nacosInstance) {
        try {
            McpMaintainerService service = buildMcpService(nacosInstance);
            // 尝试获取一个简单的列表来测试连接
            service.listMcpServer("public", "", 1, 1);
            log.info("测试Nacos连接成功: {}", nacosInstance.getServerUrl());
            return true;
        } catch (Exception e) {
            log.error("Nacos连接测试失败: {}", e.getMessage(), e);
            return false;
        }
    }



    /**
     * 获取Nacos中的MCP Server列表
     */
    public List<NacosMCPServerResult> fetchMcpServers(NacosInstance nacosInstance) {
        try {
            McpMaintainerService service = buildMcpService(nacosInstance);
            String namespace = nacosInstance.getNamespace() != null ? nacosInstance.getNamespace() : "public";
            
            Page<McpServerBasicInfo> page = service.listMcpServer(namespace, "", 1, 100);
            if (page == null || page.getPageItems() == null) {
                log.warn("获取MCP Server列表返回空，namespace: {}", namespace);
                return new ArrayList<>();
            }
            
            List<NacosMCPServerResult> results = new ArrayList<>();
            for (McpServerBasicInfo basicInfo : page.getPageItems()) {
                NacosMCPServerResult result = new NacosMCPServerResult().convertFrom(basicInfo);
                results.add(result);
            }
            
            log.info("成功获取到 {} 个MCP Server", results.size());
            return results;
        } catch (Exception e) {
            log.error("获取Nacos MCP Server列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建McpMaintainerService
     */
    private McpMaintainerService buildMcpService(NacosInstance nacosInstance) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", nacosInstance.getServerUrl());
        if (nacosInstance.getUsername() != null) {
            properties.setProperty("username", nacosInstance.getUsername());
        }
        if (nacosInstance.getPassword() != null) {
            properties.setProperty("password", nacosInstance.getPassword());
        }
        if (nacosInstance.getNamespace() != null) {
            properties.setProperty("namespace", nacosInstance.getNamespace());
        }
        properties.setProperty("contextPath", "nacos");
        return (McpMaintainerService) AiMaintainerFactory.createAiMaintainerService(properties);
    }

    /**
     * 更新Nacos实例状态
     */
    public NacosStatus updateStatus(NacosInstance nacosInstance) {
        if (testConnection(nacosInstance)) {
            return NacosStatus.RUNNING;
        } else {
            return NacosStatus.ERROR;
        }
    }
} 