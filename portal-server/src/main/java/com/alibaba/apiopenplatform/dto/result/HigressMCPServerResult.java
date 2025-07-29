package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.higress.sdk.model.mcp.McpServer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HigressMCPServerResult extends MCPServerResult implements OutputConverter<HigressMCPServerResult, McpServer> {

    @Override
    public HigressMCPServerResult convertFrom(McpServer mcpServer) {
        HigressMCPServerResult r = OutputConverter.super.convertFrom(mcpServer);

        r.setFromType(mcpServer.getType().name());
        r.setMcpServerConfig(mcpServer.getRawConfigurations());
        r.setFromType(GatewayType.HIGRESS.getType());

        r.setDomains(mcpServer.getDomains().stream().map(domain -> Domain.builder()
                .domain(domain)
                .protocol("HTTPS")
                .build())
                .collect(Collectors.toList()));

        return r;
    }
}