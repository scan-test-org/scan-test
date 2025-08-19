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
import com.alibaba.apiopenplatform.dto.converter.NacosToGatewayToolsConverter;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
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
    public NacosResult getNacosInstance(String nacosId) {
        NacosInstance nacosInstance = findNacosInstance(nacosId);
        return new NacosResult().convertFrom(nacosInstance);
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

    private NacosInstance findNacosInstance(String nacosId) {
        return nacosInstanceRepository.findByNacosId(nacosId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.NACOS_INSTANCE, nacosId));
    }

    private McpMaintainerService buildDynamicMcpService(NacosInstance nacosInstance) {
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

    private MCPConfigResult buildMCPConfigResult(McpServerDetailInfo detail) {
        MCPConfigResult mcpConfig = new MCPConfigResult();
        mcpConfig.setMcpServerName(detail.getName());

        MCPConfigResult.MCPServerConfig serverConfig = new MCPConfigResult.MCPServerConfig();
        
        if (detail.getLocalServerConfig() != null) {
            serverConfig.setRawConfig(detail.getLocalServerConfig());
        } else if (detail.getRemoteServerConfig() != null || (detail.getBackendEndpoints() != null && !detail.getBackendEndpoints().isEmpty())) {
            Object remoteConfig = buildRemoteConnectionConfig(detail);
            serverConfig.setRawConfig(remoteConfig);
        } else {
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("type", "unknown");
            defaultConfig.put("name", detail.getName());
            serverConfig.setRawConfig(defaultConfig);
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

        MCPConfigResult.McpMetadata meta = new MCPConfigResult.McpMetadata();
        meta.setSource(SourceType.NACOS.name());
        mcpConfig.setMeta(meta);

        return mcpConfig;
    }
    
    private Object buildRemoteConnectionConfig(McpServerDetailInfo detail) {
        List<?> backendEndpoints = detail.getBackendEndpoints();
        
        if (backendEndpoints != null && !backendEndpoints.isEmpty()) {
            Object firstEndpoint = backendEndpoints.get(0);
            
            Map<String, Object> connectionConfig = new HashMap<>();
            Map<String, Object> mcpServers = new HashMap<>();
            Map<String, Object> serverConfig = new HashMap<>();

            String endpointUrl = extractEndpointUrl(firstEndpoint);
            if (endpointUrl != null) {
                serverConfig.put("url", endpointUrl);
            }

            mcpServers.put(detail.getName(), serverConfig);
            connectionConfig.put("mcpServers", mcpServers);

            return connectionConfig;
        }

        Map<String, Object> basicConfig = new HashMap<>();
        basicConfig.put("type", "remote");
        basicConfig.put("name", detail.getName());
        basicConfig.put("protocol", "http");
        return basicConfig;
    }
    
    private String extractEndpointUrl(Object endpoint) {
        if (endpoint == null) {
            return null;
        }

        if (endpoint instanceof String) {
            return (String) endpoint;
        }

        if (endpoint instanceof Map) {
            Map<?, ?> endpointMap = (Map<?, ?>) endpoint;
            
            String url = getStringValue(endpointMap, "url");
            if (url != null) return url;

            String endpointUrl = getStringValue(endpointMap, "endpointUrl");
            if (endpointUrl != null) return endpointUrl;

            String host = getStringValue(endpointMap, "host");
            String port = getStringValue(endpointMap, "port");
            String path = getStringValue(endpointMap, "path");
            
            if (host != null) {
                StringBuilder urlBuilder = new StringBuilder();
                String protocol = getStringValue(endpointMap, "protocol");
                urlBuilder.append(protocol != null ? protocol : "http").append("://");
                urlBuilder.append(host);
                
                if (port != null && !port.isEmpty()) {
                    urlBuilder.append(":").append(port);
                }
                
                if (path != null && !path.isEmpty()) {
                    if (!path.startsWith("/")) {
                        urlBuilder.append("/");
                    }
                    urlBuilder.append(path);
                }
                
                return urlBuilder.toString();
            }
        }

        if (endpoint.getClass().getName().contains("McpEndpointInfo")) {
            return extractUrlFromMcpEndpointInfo(endpoint);
        }

        return endpoint.toString();
    }

    private String getStringValue(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String extractUrlFromMcpEndpointInfo(Object endpoint) {
        String[] possibleFieldNames = {"url", "endpointUrl", "address", "host", "endpoint"};
        
        for (String fieldName : possibleFieldNames) {
            try {
                java.lang.reflect.Field field = endpoint.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(endpoint);
                if (value != null && !value.toString().trim().isEmpty()) {
                    if (value.toString().contains("://") || value.toString().contains(":")) {
                        return value.toString();
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        java.lang.reflect.Field[] fields = endpoint.getClass().getDeclaredFields();
        
        String host = null;
        String port = null;
        String path = null;
        String protocol = null;
        
        for (java.lang.reflect.Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(endpoint);
                if (value != null && !value.toString().trim().isEmpty()) {
                    String fieldName = field.getName().toLowerCase();
                    
                    if (fieldName.contains("host") || fieldName.contains("ip") || fieldName.contains("address")) {
                        host = value.toString();
                    } else if (fieldName.contains("port")) {
                        port = value.toString();
                    } else if (fieldName.contains("path") || fieldName.contains("endpoint") || fieldName.contains("uri")) {
                        path = value.toString();
                    } else if (fieldName.contains("protocol") || fieldName.contains("scheme")) {
                        protocol = value.toString();
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        if (host != null) {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(protocol != null ? protocol : "http").append("://");
            urlBuilder.append(host);
            
            if (port != null && !port.isEmpty()) {
                urlBuilder.append(":").append(port);
            }
            
            if (path != null && !path.isEmpty()) {
                if (!path.startsWith("/")) {
                    urlBuilder.append("/");
                }
                urlBuilder.append(path);
            }
            
            return urlBuilder.toString();
        }
        
        return endpoint.toString();
    }
}