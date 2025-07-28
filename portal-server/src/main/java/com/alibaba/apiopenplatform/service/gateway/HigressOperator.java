package com.alibaba.apiopenplatform.service.gateway;

import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.service.gateway.client.HigressClient;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.higress.sdk.model.PaginatedResult;
import com.alibaba.higress.sdk.model.mcp.McpServer;
import com.alibaba.higress.sdk.model.mcp.McpServerPageQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public PageResult<? extends MCPServerResult> fetchMcpServers(Gateway gateway, Pageable pageable) {
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
    public String fetchAPISpec(Gateway gateway, String apiId) {

        return null;
    }

    @Override
    public String fetchMcpSpec(Gateway gateway, String apiId, String routeId, String name) {
        HigressClient client = getClient(gateway);

        HigressMCPServerResult mcpServerResult = client.execute(c -> {
            McpServer mcpServer = c.mcpServerService().query(name);
            return new HigressMCPServerResult().convertFrom(mcpServer);
        });

        return JSONUtil.toJsonStr(mcpServerResult);
    }

    @Override
    public PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable) {
        throw new UnsupportedOperationException("Higress gateway does not support fetching Gateways");
    }

    @Override
    public void createConsumer(Gateway gateway) {
    }

    @Override
    public void deleteConsumer(Gateway gateway) {

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
