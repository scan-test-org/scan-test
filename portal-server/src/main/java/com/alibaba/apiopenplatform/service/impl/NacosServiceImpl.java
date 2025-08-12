package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.nacos.CreateNacosParam;
import com.alibaba.apiopenplatform.dto.params.nacos.UpdateNacosParam;
import com.alibaba.apiopenplatform.dto.result.NacosMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.NacosResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.MCPConfigResult;
import com.alibaba.apiopenplatform.entity.NacosInstance;
import com.alibaba.apiopenplatform.repository.NacosInstanceRepository;
import com.alibaba.apiopenplatform.service.NacosService;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import com.alibaba.apiopenplatform.converter.NacosToGatewayToolsConverter;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.McpMaintainerService;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Nacos服务实现
 *
 * @author zxd
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NacosServiceImpl implements NacosService {

    private static final String DEFAULT_CONTEXT_PATH = "nacos";

    private final NacosInstanceRepository nacosInstanceRepository;

    private final ContextHolder contextHolder;

    @Override
    public PageResult<NacosResult> listNacosInstances(Pageable pageable) {
        Page<NacosInstance> nacosInstances = nacosInstanceRepository.findAll(pageable);
        return new PageResult<NacosResult>().convertFrom(nacosInstances, nacosInstance -> new NacosResult().convertFrom(nacosInstance));
    }

    @Override
    public void createNacosInstance(CreateNacosParam param) {
        nacosInstanceRepository.findByNacosName(param.getNacosName())
                .ifPresent(nacos -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.NACOS_INSTANCE, nacos.getNacosName());
                });

        NacosInstance nacosInstance = param.convertTo();
        nacosInstance.setNacosId(IdGenerator.genNacosId());
        nacosInstance.setAdminId(contextHolder.getUser());

        nacosInstanceRepository.save(nacosInstance);
    }

    @Override
    public void updateNacosInstance(String nacosId, UpdateNacosParam param) {
        NacosInstance instance = findNacosInstance(nacosId);

        Optional.ofNullable(param.getNacosName())
                .filter(name -> !name.equals(instance.getNacosName()))
                .flatMap(nacosInstanceRepository::findByNacosName)
                .ifPresent(nacos -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.NACOS_INSTANCE, nacos.getNacosName());
                });

        param.update(instance);
        nacosInstanceRepository.saveAndFlush(instance);
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

        McpMaintainerService service = buildDynamicMcpService(nacosInstance);
        com.alibaba.nacos.api.model.Page<McpServerBasicInfo> page = service.listMcpServer(nacosInstance.getNamespace(), "", 1, Integer.MAX_VALUE);
        if (page == null || page.getPageItems() == null) {
            return PageResult.empty(pageable.getPageNumber(), pageable.getPageSize());
        }

        return page.getPageItems().stream()
                .map(basicInfo -> new NacosMCPServerResult().convertFrom(basicInfo))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> PageResult.of(list, pageable.getPageNumber(), pageable.getPageSize(), page.getPageItems().size())
                ));
    }

    @Override
    public String fetchMcpConfig(String nacosId, NacosRefConfig nacosRefConfig) {
        NacosInstance nacosInstance = findNacosInstance(nacosId);

        McpMaintainerService service = buildDynamicMcpService(nacosInstance);
        try {
            McpServerDetailInfo detail = service.getMcpServerDetail(nacosRefConfig.getNamespaceId(),
                    nacosRefConfig.getMcpServerName(), null);
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
            serverConfig.setRawConfig(detail.getLocalServerConfig());
        }
        mcpConfig.setMcpServerConfig(serverConfig);

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
        meta.setSource(SourceType.NACOS.name());
        mcpConfig.setMeta(meta);

        return mcpConfig;
    }

    private NacosInstance findNacosInstance(String nacosId) {
        return nacosInstanceRepository.findByNacosId(nacosId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.NACOS_INSTANCE, nacosId));
    }

    private McpMaintainerService buildDynamicMcpService(NacosInstance nacosInstance) {
        // Nacos Properties
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosInstance.getServerUrl());
        properties.setProperty(PropertyKeyConst.USERNAME, nacosInstance.getUsername());
        properties.setProperty(PropertyKeyConst.PASSWORD, nacosInstance.getPassword());
        properties.setProperty(PropertyKeyConst.CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
        properties.setProperty(PropertyKeyConst.NAMESPACE, nacosInstance.getNamespace());

        try {
            return AiMaintainerFactory.createAiMaintainerService(properties);
        } catch (Exception e) {
            log.error("Error init Nacos AiMaintainerService", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error init Nacos AiMaintainerService");
        }
    }
}