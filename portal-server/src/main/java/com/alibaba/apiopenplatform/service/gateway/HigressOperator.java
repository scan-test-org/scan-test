package com.alibaba.apiopenplatform.service.gateway;

import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.entity.ConsumerCredential;
import com.alibaba.apiopenplatform.service.gateway.client.HigressClient;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.product.HigressRefConfig;
import com.alibaba.higress.sdk.model.PaginatedResult;
import com.alibaba.higress.sdk.model.mcp.McpServer;
import com.alibaba.higress.sdk.model.mcp.McpServerPageQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;

/**
 * @author zh
 */
@Service
@Slf4j
public class HigressOperator extends GatewayOperator<HigressClient> {

    @Override
    public PageResult<APIResult> fetchHTTPAPIs(Gateway gateway, Pageable pageable) {
        throw new UnsupportedOperationException("Higress gateway does not support HTTP APIs");
    }

    @Override
    public PageResult<APIResult> fetchRESTAPIs(Gateway gateway, Pageable pageable) {
        throw new UnsupportedOperationException("Higress gateway does not support REST APIs");
    }

    @Override
    public PageResult<? extends GatewayMCPServerResult> fetchMcpServers(Gateway gateway, Pageable pageable) {
        HigressClient client = getClient(gateway);

        McpServerPageQuery query = new McpServerPageQuery();
        query.setPageNum(pageable.getPageNumber());
        query.setPageSize(pageable.getPageSize());

        return client.execute(c -> {
            PaginatedResult<McpServer> page = c.mcpServerService().list(query);
            List<HigressMCPServerResult> mcpServers = page.getData().stream()
                    .map(s -> new HigressMCPServerResult().convertFrom(s))
                    .collect(Collectors.toList());

            return PageResult.of(mcpServers, pageable.getPageNumber(), pageable.getPageSize(), page.getTotal());

        });
    }

    @Override
    public String fetchAPIConfig(Gateway gateway, Object config) {

        return null;
    }

    @Override
    public String fetchMcpConfig(Gateway gateway, Object conf) {
        HigressClient client = getClient(gateway);
        HigressRefConfig config = (HigressRefConfig) conf;

        McpServer mcpServer = client.execute(c -> c.mcpServerService().query(config.getMcpServerName()));

        MCPConfigResult m = new MCPConfigResult();
        m.setMcpServerName(mcpServer.getName());

        // mcpServer config
        MCPConfigResult.MCPServerConfig c = new MCPConfigResult.MCPServerConfig();
        c.setPath("/" + mcpServer.getName());
        c.setDomains(mcpServer.getDomains().stream().map(domain -> MCPConfigResult.Domain.builder()
                        .domain(domain)
                        .protocol("HTTPS")
                        .build())
                .collect(Collectors.toList()));
        m.setMcpServerConfig(c);

        // tools
        m.setTools(mcpServer.getRawConfigurations());

        // meta
        MCPConfigResult.McpMetadata meta = new MCPConfigResult.McpMetadata();
        meta.setSource(GatewayType.APIG_AI.name());
        meta.setFromType(mcpServer.getType().name());
        m.setMeta(meta);

        return JSONUtil.toJsonStr(m);
    }

    @Override
    public PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable) {
        throw new UnsupportedOperationException("Higress gateway does not support fetching Gateways");
    }

    @Override
    public String createConsumer(Gateway gateway, Consumer consumer, ConsumerCredential credential) {
        HigressClient client = getClient(gateway);
        try {
            // Higress网关的创建消费者逻辑
            log.info("Creating consumer {} in Higress gateway {}", consumer.getConsumerId(), gateway.getGatewayId());
            
            // TODO: 实现具体的Higress创建消费者逻辑
            // 例如：配置路由规则、权限等
            // 可以使用credential中的信息进行认证配置
            
            return "gwConsumerId"; // 示例返回值
        } catch (Exception e) {
            log.error("Error creating consumer {} in Higress gateway {}", consumer.getConsumerId(), gateway.getGatewayId(), e);
            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "Failed to create consumer in Higress gateway: " + e.getMessage());
        }
    }

    @Override
    public void deleteConsumer(Gateway gateway) {

    }

    @Override
    public void authorizationConsumerToApi(Gateway gateway, String consumerId, String apiId) {
        HigressClient client = getClient(gateway);
        try {
            // Higress网关的授权逻辑
            log.info("Authorizing consumer {} to apiId {} in Higress gateway {}", consumerId, apiId, gateway.getGatewayId());
            
            // TODO: 实现具体的Higress授权逻辑
            // 例如：配置路由规则、权限等
            
        } catch (Exception e) {
            log.error("Error authorizing consumer {} to apiId {} in Higress gateway {}", consumerId, apiId, gateway.getGatewayId(), e);
            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "Failed to authorize consumer to apiId in Higress gateway: " + e.getMessage());
        }
    }

    @Override
    public APIResult fetchAPI(Gateway gateway, String apiId) {
        return null;
    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.HIGRESS;
    }
}
