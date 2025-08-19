package com.alibaba.apiopenplatform.service.gateway;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.result.GatewayMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.service.gateway.client.APIGClient;
import com.alibaba.apiopenplatform.support.consumer.ConsumerAuthConfig;
import com.alibaba.apiopenplatform.support.enums.APIGAPIType;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.product.APIGRefConfig;
import com.aliyun.sdk.service.apig20240327.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@Service
@Slf4j
public class AIGatewayOperator extends APIGOperator {

    @Override
    public PageResult<? extends GatewayMCPServerResult> fetchMcpServers(Gateway gateway, Pageable pageable) {
        PageResult<APIResult> page = fetchAPIs(gateway, APIGAPIType.MCP, PageRequest.of(0, 1));
        if (page.getTotalElements() == 0) {
            return PageResult.empty(pageable.getPageNumber(), pageable.getPageSize());
        }

        // MCP Server定义在一个API下
        String apiId = page.getContent().get(0).getApiId();
        try {
            PageResult<HttpRoute> routesPage = fetchHttpRoutes(gateway, apiId, pageable);
            if (routesPage.getTotalElements() == 0) {
                return PageResult.empty(pageable.getPageNumber(), pageable.getPageSize());
            }

            return PageResult.<APIGMCPServerResult>builder().build()
                    .mapFrom(routesPage, route -> {
                        APIGMCPServerResult r = new APIGMCPServerResult().convertFrom(route);
                        r.setApiId(apiId);
                        return r;
                    });
        } catch (Exception e) {
            log.error("Error fetching MCP servers", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching MCP servers，Cause：" + e.getMessage());
        }
    }

    @Override
    public String fetchMcpConfig(Gateway gateway, Object conf) {
        APIGRefConfig config = (APIGRefConfig) conf;
        HttpRoute httpRoute = fetchHTTPRoute(gateway, config.getApiId(), config.getMcpRouteId());

        MCPConfigResult m = new MCPConfigResult();
        m.setMcpServerName(httpRoute.getName());

        // mcpServer config
        MCPConfigResult.MCPServerConfig c = new MCPConfigResult.MCPServerConfig();
        if (httpRoute.getMatch() != null) {
            c.setPath(httpRoute.getMatch().getPath().getValue());
        }
        if (httpRoute.getDomainInfos() != null) {
            c.setDomains(httpRoute.getDomainInfos().stream()
                    .map(domainInfo -> MCPConfigResult.Domain.builder()
                            .domain(domainInfo.getName())
                            .protocol(domainInfo.getProtocol())
                            .build())
                    .collect(Collectors.toList()));
        }
        m.setMcpServerConfig(c);

        // meta
        MCPConfigResult.McpMetadata meta = new MCPConfigResult.McpMetadata();
        meta.setSource(GatewayType.APIG_AI.name());

        // tools
        HttpRoute.McpServerInfo mcpServerInfo = httpRoute.getMcpServerInfo();
        boolean fetchTool = true;
        if (mcpServerInfo.getMcpRouteConfig() != null) {
            String protocol = mcpServerInfo.getMcpRouteConfig().getProtocol();
            meta.setFromType(protocol);

            // HTTP转MCP需从插件获取tools配置
            fetchTool = StrUtil.equalsIgnoreCase(protocol, "HTTP");
        }

        if (fetchTool) {
            String toolSpec = fetchMcpTools(gateway, config.getMcpRouteId());
            if (StrUtil.isNotBlank(toolSpec)) {
                m.setTools(toolSpec);
                // 默认为HTTP转MCP
                if (StrUtil.isBlank(meta.getFromType())) {
                    meta.setFromType("HTTP");
                }
            }
        }

        m.setMeta(meta);
        return JSONUtil.toJsonStr(m);
    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.APIG_AI;
    }

    public String fetchMcpTools(Gateway gateway, String routeId) {
        APIGClient client = getClient(gateway);

        try {
            ListPluginAttachmentsResponse response = client.execute(c -> {
                ListPluginAttachmentsRequest request = ListPluginAttachmentsRequest.builder()
                        .gatewayId(gateway.getGatewayId())
                        .attachResourceId(routeId)
                        .attachResourceType("GatewayRoute")
                        .pageNumber(1)
                        .pageSize(100)
                        .build();
                try {
                    return c.listPluginAttachments(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            if (response.getStatusCode() != 200) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }

            for (ListPluginAttachmentsResponseBody.Items item : response.getBody().getData().getItems()) {
                PluginClassInfo classInfo = item.getPluginClassInfo();

                if (!StrUtil.equalsIgnoreCase(classInfo.getName(), "mcp-server")) {
                    continue;
                }

                String pluginConfig = item.getPluginConfig();
                if (StrUtil.isNotBlank(pluginConfig)) {
                    return Base64.decodeStr(pluginConfig);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Plugin Attachment", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching Plugin Attachment，Cause：" + e.getMessage());
        }
        return null;
    }

    @Override
    public ConsumerAuthConfig authorizeConsumer(Gateway gateway, String consumerId, Object refConfig) {
        APIGClient client = getClient(gateway);

        APIGRefConfig config = (APIGRefConfig) refConfig;
        // MCP Server 授权
        String mcpRouteId = config.getMcpRouteId();

        try {
            // 确认Gateway的EnvId
            String envId = fetchGatewayEnv(gateway);

            CreateConsumerAuthorizationRulesRequest.AuthorizationRules rule = CreateConsumerAuthorizationRulesRequest.AuthorizationRules.builder()
                    .consumerId(consumerId)
                    .expireMode("LongTerm")
                    .resourceType("MCP")
                    .resourceIdentifier(CreateConsumerAuthorizationRulesRequest.ResourceIdentifier.builder()
                            .resourceId(mcpRouteId)
                            .environmentId(envId).build())
                    .build();

            CreateConsumerAuthorizationRulesResponse response = client.execute(c -> {
                CreateConsumerAuthorizationRulesRequest request = CreateConsumerAuthorizationRulesRequest.builder()
                        .authorizationRules(Collections.singletonList(rule))
                        .build();
                try {
                    return c.createConsumerAuthorizationRules(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            if (200 != response.getStatusCode()) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }

            return ConsumerAuthConfig.builder()
                    .apigAuthorizationRuleIds(response.getBody().getData().getConsumerAuthorizationRuleIds())
                    .build();
        } catch (Exception e) {
            log.error("Error authorizing consumer {} to mcp server {} in AI gateway {}", consumerId, mcpRouteId, gateway.getGatewayId(), e);
            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "Failed to authorize consumer to mcp server in AI gateway: " + e.getMessage());
        }
    }
}
