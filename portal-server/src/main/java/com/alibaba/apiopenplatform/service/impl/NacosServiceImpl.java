package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketDetailParam;
import com.alibaba.apiopenplatform.dto.params.nacos.CreateNacosParam;
import com.alibaba.apiopenplatform.dto.params.nacos.UpdateNacosParam;
import com.alibaba.apiopenplatform.dto.result.NacosMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.NacosResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.MCPConfigResult;
import com.alibaba.apiopenplatform.entity.NacosInstance;
import com.alibaba.apiopenplatform.repository.NacosInstanceRepository;
import com.alibaba.apiopenplatform.service.NacosService;
import com.alibaba.apiopenplatform.service.gateway.NacosOperator;

import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.McpMaintainerService;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;

import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;

/**
 * Nacos服务实现
 * @author zxd
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NacosServiceImpl implements NacosService {

    private final NacosInstanceRepository nacosInstanceRepository;
    private final NacosOperator nacosOperator;

    @Value("${nacos.maintainer.server-addr:}")
    private String serverAddr;
    @Value("${nacos.maintainer.username:}")
    private String username;
    @Value("${nacos.maintainer.password:}")
    private String password;

    private McpMaintainerService mcpMaintainerService;

    @PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", serverAddr);
            properties.setProperty("username", username);
            properties.setProperty("password", password);
            properties.setProperty("contextPath", "nacos");
            mcpMaintainerService = (McpMaintainerService) AiMaintainerFactory.createAiMaintainerService(properties);
        } catch (Exception e) {
            log.error("Failed to init McpMaintainerService", e);
        }
    }



    @Override
    public McpMarketDetailParam getMcpServerDetail(String nacosId, String mcpName, String namespaceId, String version) {
        log.info("开始获取MCP Server详情，nacosId: {}, mcpName: {}, namespaceId: {}, version: {}", 
                nacosId, mcpName, namespaceId, version);
        try {
            // 根据nacosId查找Nacos实例并构建McpMaintainerService
            NacosInstance nacosInstance = findNacosInstance(nacosId);
            McpMaintainerService serviceToUse = buildDynamicMcpService(nacosInstance);
            
            McpServerDetailInfo detail = serviceToUse.getMcpServerDetail(namespaceId, mcpName, version);
            if (detail == null) {
                log.warn("getMcpServerDetail 返回 null，namespaceId: {}, mcpName: {}, version: {}", namespaceId, mcpName, version);
                return null;
            }
            log.info("成功获取到详情数据，id: {}, name: {}", detail.getId(), detail.getName());
            return McpMarketDetailParam.builder().build().convertFrom(detail);
        } catch (Exception e) {
            log.error("获取MCP Server详情时发生异常，nacosId: {}, mcpName: {}, namespaceId: {}, version: {}", 
                    nacosId, mcpName, namespaceId, version, e);
            return null;
        }
    }

    private McpMaintainerService buildDynamicMcpService(NacosInstance nacosInstance) throws Exception {
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

    @Override
    public PageResult<NacosResult> listNacosInstances(Pageable pageable) {
        Page<NacosInstance> nacosInstances = nacosInstanceRepository.findByAdminId("admin", pageable);
        
        return new PageResult<NacosResult>().convertFrom(nacosInstances, nacosInstance -> new NacosResult().convertFrom(nacosInstance));
    }

    @Override
    public void createNacosInstance(CreateNacosParam param) {
        // 检查名称是否已存在
        nacosInstanceRepository.findByNacosNameAndAdminId(param.getNacosName(), "admin")
            .ifPresent(nacos -> {
                throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.NACOS_INSTANCE, nacos.getNacosName());
            });

        NacosInstance nacosInstance = param.convertTo();
        nacosInstance.setNacosId(IdGenerator.genNacosId());
        nacosInstance.setAdminId("admin");

        nacosInstanceRepository.save(nacosInstance);
    }

    @Override
    public void updateNacosInstance(String nacosId, UpdateNacosParam param) {
        NacosInstance existingInstance = findNacosInstance(nacosId);
        
        // 检查名称是否已被其他实例使用
        nacosInstanceRepository.findByNacosNameAndAdminId(param.getNacosName(), "admin")
            .ifPresent(nacos -> {
                if (!nacos.getNacosId().equals(nacosId)) {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.NACOS_INSTANCE, nacos.getNacosName());
                }
            });

        // 更新字段
        param.update(existingInstance);

        nacosInstanceRepository.save(existingInstance);
    }

    @Override
    public void deleteNacosInstance(String nacosId) {
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        nacosInstanceRepository.delete(nacosInstance);
    }

    @Override
    public NacosResult getNacosInstance(String nacosId) {
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        return new NacosResult().convertFrom(nacosInstance);
    }

    @Override
    public PageResult<NacosMCPServerResult> fetchMcpServers(String nacosId, Pageable pageable) throws Exception {
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        return nacosOperator.fetchMcpServers(nacosInstance, pageable);
    }

    @Override
    public String fetchMcpConfig(String nacosId, Object conf) throws Exception {
        NacosRefConfig config = (NacosRefConfig) conf;
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        
        McpMaintainerService service = buildDynamicMcpService(nacosInstance);
        String namespace = config.getNamespaceId() != null ? config.getNamespaceId() : "public";
        String version = null; // 使用null，因为NacosRefConfig中已删除version字段

        McpServerDetailInfo detail = service.getMcpServerDetail(namespace, config.getMcpServerName(), version);
        if (detail == null) {
            return null;
        }

        MCPConfigResult m = new MCPConfigResult();
        m.setMcpServerName(detail.getName());

        // mcpServer config
        MCPConfigResult.MCPServerConfig c = new MCPConfigResult.MCPServerConfig();
        if (detail.getLocalServerConfig() != null) {
            c.setRawConfig(detail.getLocalServerConfig().toString());
        }
        m.setMcpServerConfig(c);

        // tools
        if (detail.getToolSpec() != null) {
            String toolJson = JSONUtil.toJsonStr(detail.getToolSpec());
            if (toolJson == null || toolJson.trim().equals("{}")) {
                m.setTools(null);
            } else {
                m.setTools(toolJson);
            }
        } else {
            m.setTools(null);
        }

        // meta
        MCPConfigResult.McpMetadata meta = new MCPConfigResult.McpMetadata();
        meta.setSource("nacos");
        m.setMeta(meta);

        return JSONUtil.toJsonStr(m);
    }

    @Override
    public NacosInstance findNacosInstance(String nacosId) {
        return nacosInstanceRepository.findByNacosId(nacosId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.NACOS_INSTANCE, nacosId));
    }


} 