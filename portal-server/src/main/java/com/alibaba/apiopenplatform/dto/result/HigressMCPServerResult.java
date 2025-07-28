package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.higress.sdk.model.mcp.McpServer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HigressMCPServerResult extends MCPServerResult implements OutputConverter<HigressMCPServerResult, McpServer> {

    private List<String> domains;

    private String type;

    private String from = GatewayType.HIGRESS.getType();
}