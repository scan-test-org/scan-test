package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Nacos MCP Server结果
 * @author zxd
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NacosMCPServerResult extends MCPServerResult implements OutputConverter<NacosMCPServerResult, McpServerBasicInfo> {

    private String version;

    @Override
    public NacosMCPServerResult convertFrom(McpServerBasicInfo basicInfo) {
        NacosMCPServerResult r = OutputConverter.super.convertFrom(basicInfo);
        r.setMcpServerName(basicInfo.getName());
        r.setVersion(basicInfo.getVersion());
        return this;
    }
} 