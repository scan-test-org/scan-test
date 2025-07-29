package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import com.aliyun.sdk.service.apig20240327.models.HttpRouteMatch;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class APIGMCPServerResult extends MCPServerResult implements OutputConverter<APIGMCPServerResult, HttpRoute> {

    private String apiId;

    private String routeId;

    @Override
    public APIGMCPServerResult convertFrom(HttpRoute httpRoute) {
        APIGMCPServerResult r = OutputConverter.super.convertFrom(httpRoute);

        HttpRoute.McpServerInfo mcpServerInfo = httpRoute.getMcpServerInfo();
        r.setFromType(mcpServerInfo.getCreateFromType());

        r.setDomains(httpRoute.getDomainInfos().stream()
                .map(domainInfo -> Domain.builder()
                        .domain(domainInfo.getName())
                        .protocol(domainInfo.getProtocol())
                        .build())
                .collect(Collectors.toList()));
        r.setMcpServerConfig(mcpServerInfo.getMcpServerConfig());
        r.setFromType(GatewayType.APIG_AI.getType());
        return r;
    }
}
