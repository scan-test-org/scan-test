package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

/**
 * Nacos MCP Server结果
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NacosMCPServerResult extends MCPServerResult implements OutputConverter<NacosMCPServerResult, McpServerBasicInfo> {

    private String namespace;

    private String version;
    
    private String protocol;
    
    private String description;
    
    private Object capabilities;
    
    private boolean enabled;
    
    private Object backendEndpoints;

    @Override
    public NacosMCPServerResult convertFrom(McpServerBasicInfo basicInfo) {
        NacosMCPServerResult r = OutputConverter.super.convertFrom(basicInfo);
        r.setMcpServerName(basicInfo.getName());
        return r;
    }
} 