package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.higress.sdk.model.mcp.McpServer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HigressMCPServerResult extends GatewayMCPServerResult implements OutputConverter<HigressMCPServerResult, McpServer> {

    @Override
    public HigressMCPServerResult convertFrom(McpServer mcpServer) {
        HigressMCPServerResult r = OutputConverter.super.convertFrom(mcpServer);
        r.setMcpServerName(mcpServer.getName());
        return r;
    }
}