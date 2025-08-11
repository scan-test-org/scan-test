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
import com.alibaba.apiopenplatform.converter.NacosToGatewayToolsConverter;
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

import java.util.Properties;
import javax.annotation.PostConstruct;

/**
 * Nacos服务实现
 *
 * @author zxd
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NacosServiceImpl implements NacosService {

    private static final String DEFAULT_ADMIN_ID = "admin";
    private static final String DEFAULT_CONTEXT_PATH = "nacos";

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
            Properties properties = buildProperties(serverAddr, username, password);
            mcpMaintainerService = (McpMaintainerService) AiMaintainerFactory.createAiMaintainerService(properties);
        } catch (Exception e) {
            // Failed to init McpMaintainerService
        }
    }

    @Override
    public McpMarketDetailParam getMcpServerDetail(String nacosId, String mcpName, String namespaceId, String version) {
        try {
            NacosInstance nacosInstance = findNacosInstance(nacosId);
            McpMaintainerService serviceToUse = buildDynamicMcpService(nacosInstance);

            McpServerDetailInfo detail = serviceToUse.getMcpServerDetail(namespaceId, mcpName, version);
            return detail != null ? McpMarketDetailParam.builder().build().convertFrom(detail) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private McpMaintainerService buildDynamicMcpService(NacosInstance nacosInstance) {
        Properties properties = buildProperties(
                nacosInstance.getServerUrl(),
                nacosInstance.getUsername(),
                nacosInstance.getPassword()
        );
        if (nacosInstance.getNamespace() != null) {
            properties.setProperty("namespace", nacosInstance.getNamespace());
        }
        try {
            return AiMaintainerFactory.createAiMaintainerService(properties);
        } catch (Exception e) {
            log.error("Error init Nacos McpMaintainerService", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to init Nacos McpMaintainerService");
        }
    }

    private Properties buildProperties(String serverAddr, String username, String password) {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverAddr);
        if (username != null) {
            properties.setProperty("username", username);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }
        properties.setProperty("contextPath", DEFAULT_CONTEXT_PATH);
        return properties;
    }

    @Override
    public PageResult<NacosResult> listNacosInstances(Pageable pageable) {
        Page<NacosInstance> nacosInstances = nacosInstanceRepository.findByAdminId(DEFAULT_ADMIN_ID, pageable);
        return new PageResult<NacosResult>().convertFrom(nacosInstances, nacosInstance -> new NacosResult().convertFrom(nacosInstance));
    }

    @Override
    public void createNacosInstance(CreateNacosParam param) {
        nacosInstanceRepository.findByNacosNameAndAdminId(param.getNacosName(), DEFAULT_ADMIN_ID)
                .ifPresent(nacos -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.NACOS_INSTANCE, nacos.getNacosName());
                });

        NacosInstance nacosInstance = param.convertTo();
        nacosInstance.setNacosId(IdGenerator.genNacosId());
        nacosInstance.setAdminId(DEFAULT_ADMIN_ID);
        nacosInstanceRepository.save(nacosInstance);
    }

    @Override
    public void updateNacosInstance(String nacosId, UpdateNacosParam param) {
        NacosInstance existingInstance = findNacosInstance(nacosId);

        nacosInstanceRepository.findByNacosNameAndAdminId(param.getNacosName(), DEFAULT_ADMIN_ID)
                .ifPresent(nacos -> {
                    if (!nacos.getNacosId().equals(nacosId)) {
                        throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.NACOS_INSTANCE, nacos.getNacosName());
                    }
                });

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
    public String fetchMcpConfig(String nacosId, Object conf) {
        NacosRefConfig config = (NacosRefConfig) conf;
        NacosInstance nacosInstance = findNacosInstance(nacosId);

        McpMaintainerService service = buildDynamicMcpService(nacosInstance);
        String namespace = config.getNamespaceId() != null ? config.getNamespaceId() : "public";
        String version = null;

        try {
            McpServerDetailInfo detail = service.getMcpServerDetail(namespace, config.getMcpServerName(), version);
            if (detail == null) {
                return null;
            }

            MCPConfigResult mcpConfig = buildMCPConfigResult(detail);
            return JSONUtil.toJsonStr(mcpConfig);
        } catch (Exception e) {
            log.error("Error fetching Nacos MCP servers", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to fetch Nacos MCP config");
        }
    }

    private MCPConfigResult buildMCPConfigResult(McpServerDetailInfo detail) {
        MCPConfigResult mcpConfig = new MCPConfigResult();
        mcpConfig.setMcpServerName(detail.getName());

        // mcpServer config
        MCPConfigResult.MCPServerConfig serverConfig = new MCPConfigResult.MCPServerConfig();
        if (detail.getLocalServerConfig() != null) {
            serverConfig.setRawConfig(detail.getLocalServerConfig().toString());
        }
        mcpConfig.setMcpServerConfig(serverConfig);

        // tools - 使用转换器转换为网关格式
        if (detail.getToolSpec() != null) {
            try {
                NacosToGatewayToolsConverter converter = new NacosToGatewayToolsConverter();
                converter.convertFromNacos(detail);
                String gatewayFormatYaml = converter.toYaml();
                mcpConfig.setTools(gatewayFormatYaml);
            } catch (Exception e) {
                log.error("Error converting tools to gateway format", e);
                mcpConfig.setTools(null);
            }
        } else {
            mcpConfig.setTools(null);
        }

        // meta
        MCPConfigResult.McpMetadata meta = new MCPConfigResult.McpMetadata();
        meta.setSource("nacos");
        mcpConfig.setMeta(meta);

        return mcpConfig;
    }



    @Override
    public NacosInstance findNacosInstance(String nacosId) {
        return nacosInstanceRepository.findByNacosId(nacosId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.NACOS_INSTANCE, nacosId));
    }
} 