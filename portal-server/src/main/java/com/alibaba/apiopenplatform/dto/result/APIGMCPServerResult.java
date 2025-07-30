package com.alibaba.apiopenplatform.dto.result;

import cn.hutool.core.collection.CollUtil;
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

    private String mcpRouteId;

    @Override
    public APIGMCPServerResult convertFrom(HttpRoute httpRoute) {
        APIGMCPServerResult r = OutputConverter.super.convertFrom(httpRoute);

        r.setMcpServerName(httpRoute.getName());
        r.setMcpRouteId(httpRoute.getRouteId());
        r.setFromGatewayType(GatewayType.APIG_AI.name());

        if (CollUtil.isNotEmpty(httpRoute.getDomainInfos())) {
            r.setDomains(httpRoute.getDomainInfos().stream()
                    .map(domainInfo -> Domain.builder()
                            .domain(domainInfo.getName())
                            .protocol(domainInfo.getProtocol())
                            .build())
                    .collect(Collectors.toList()));
        }

        HttpRoute.McpServerInfo mcpServerInfo = httpRoute.getMcpServerInfo();
        if (mcpServerInfo != null) {
            r.setFromType(mcpServerInfo.getCreateFromType());
            r.setMcpServerConfig(mcpServerInfo.getMcpServerConfig());
        }
        return r;
    }
}
