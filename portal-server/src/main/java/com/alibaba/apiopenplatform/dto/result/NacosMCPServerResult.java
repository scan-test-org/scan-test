package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zxd
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NacosMCPServerResult extends MCPServerResult implements OutputConverter<NacosMCPServerResult, McpServerBasicInfo> {

    private String version;

    @Override
    public NacosMCPServerResult convertFrom(McpServerBasicInfo basicInfo) {
        OutputConverter.super.convertFrom(basicInfo);
        setMcpServerName(basicInfo.getName());
        setVersion(basicInfo.getVersion());
        return this;
    }
} 