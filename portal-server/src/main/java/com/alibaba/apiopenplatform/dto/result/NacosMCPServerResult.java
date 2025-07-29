package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

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

        // 设置来源信息
        r.setFromType("NACOS");
        r.setName(basicInfo.getName());
        
        
        // 设置MCP Server配置信息
        if (basicInfo.getLocalServerConfig() != null) {
            r.setMcpServerConfig(basicInfo.getLocalServerConfig().toString());
        }
        
        // 设置domains信息 - Nacos MCP Server没有实际的域名，保持为空
        r.setDomains(new ArrayList<>());
        
        return r;
    }
} 