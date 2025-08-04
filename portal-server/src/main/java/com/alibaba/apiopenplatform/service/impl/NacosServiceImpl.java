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
import com.alibaba.apiopenplatform.support.enums.NacosStatus;
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
            param.setNamespaceId(null);
            param.setMcpName(detail.getName());
            return param;
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
        
        return new PageResult<NacosResult>().convertFrom(nacosInstances, 
            nacosInstance -> new NacosResult().convertFrom(nacosInstance));
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

        // 测试连接并更新状态
        NacosStatus status = nacosOperator.updateStatus(nacosInstance);
        nacosInstance.setStatus(status);

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
        existingInstance.setNacosName(param.getNacosName());
        existingInstance.setServerUrl(param.getServerUrl());
        existingInstance.setNamespace(param.getNamespace());
        existingInstance.setUsername(param.getUsername());
        existingInstance.setPassword(param.getPassword());
        existingInstance.setDescription(param.getDescription());

        // 测试连接并更新状态
        NacosStatus status = nacosOperator.updateStatus(existingInstance);
        existingInstance.setStatus(status);

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
    public PageResult<NacosMCPServerResult> fetchMcpServers(String nacosId, Pageable pageable) {
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        
        List<NacosMCPServerResult> mcpServers = nacosOperator.fetchMcpServers(nacosInstance);
        
        // 简单的分页处理
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), mcpServers.size());
        
        List<NacosMCPServerResult> pageContent = mcpServers.subList(start, end);
        
        return PageResult.of(pageContent, pageable.getPageNumber(), pageable.getPageSize(), mcpServers.size());
    }

    @Override
    public String fetchMcpConfig(String nacosId, Object conf) {
        NacosRefConfig config = (NacosRefConfig) conf;
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        
        try {
            McpMaintainerService service = buildDynamicMcpService(nacosInstance);
            String namespace = config.getNamespaceId() != null ? config.getNamespaceId() : "public";
            String version = config.getVersion();
            
            McpServerDetailInfo detail = service.getMcpServerDetail(namespace, config.getMcpServerName(), version);
            if (detail == null) {
                log.warn("getMcpServerDetail 返回 null，namespace: {}, mcpName: {}, version: {}", namespace, config.getMcpServerName(), version);
                return null;
            }
            
            MCPConfigResult m = new MCPConfigResult();
            m.setMcpServerName(detail.getName());
            
            // mcpServer config
            MCPConfigResult.MCPServerConfig c = new MCPConfigResult.MCPServerConfig();
            if (detail.getLocalServerConfig() != null) {
                c.setLocalConfig(detail.getLocalServerConfig().toString());
            }
            m.setMcpServerConfig(c);
            
            // tools
            if (detail.getToolSpec() != null) {
                m.setTools(detail.getToolSpec().toString());
            }
            
            // meta
            MCPConfigResult.McpMetadata meta = new MCPConfigResult.McpMetadata();
            meta.setSource("nacos");
            m.setMeta(meta);
            
            return JSONUtil.toJsonStr(m);
        } catch (Exception e) {
            log.error("获取Nacos MCP配置失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public NacosInstance findNacosInstance(String nacosId) {
        return nacosInstanceRepository.findByNacosId(nacosId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.NACOS_INSTANCE, nacosId));
    }

    @Override
    public boolean testConnection(String nacosId) {
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        return nacosOperator.testConnection(nacosInstance);
    }
} 