package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Nacos MCP Server结果
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NacosMCPServerResult extends MCPServerResult implements OutputConverter<NacosMCPServerResult, McpServerBasicInfo> {

    private String namespace;

    private String version;

    @Override
    public NacosMCPServerResult convertFrom(McpServerBasicInfo basicInfo) {
        NacosMCPServerResult r = OutputConverter.super.convertFrom(basicInfo);

        // 设置来源信息
        r.setFromType("NACOS");
        r.setName(basicInfo.getName());
        
        // 设置命名空间和版本信息（如果有的话）
        // 注意：这里需要根据McpServerBasicInfo的实际字段来设置
        // 如果字段不存在，可以注释掉或使用其他方式获取
        
        return r;
    }
} 