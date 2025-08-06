package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.dto.result.NacosMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.NacosInstance;

import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.McpMaintainerService;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.model.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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
     * 获取Nacos中的MCP Server列表
     */
    public PageResult<NacosMCPServerResult> fetchMcpServers(NacosInstance nacosInstance, Pageable pageable) throws Exception {
        McpMaintainerService service = buildMcpService(nacosInstance);
        String namespace = nacosInstance.getNamespace() != null ? nacosInstance.getNamespace() : "public";
        
        Page<McpServerBasicInfo> page = service.listMcpServer(namespace, "", 1, 100);
        if (page == null || page.getPageItems() == null) {
            return PageResult.of(new ArrayList<>(), pageable.getPageNumber(), pageable.getPageSize(), 0);
        }
        
        return page.getPageItems().stream()
                .map(basicInfo -> new NacosMCPServerResult().convertFrom(basicInfo))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> PageResult.of(list, pageable.getPageNumber(), pageable.getPageSize(), page.getPageItems().size())
                ));
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


} 