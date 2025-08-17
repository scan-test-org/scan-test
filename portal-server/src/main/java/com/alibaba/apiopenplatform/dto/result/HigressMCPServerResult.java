package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.service.gateway.HigressOperator.HigressMCPConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HigressMCPServerResult extends GatewayMCPServerResult implements OutputConverter<HigressMCPServerResult, HigressMCPConfig> {

    @Override
    public HigressMCPServerResult convertFrom(HigressMCPConfig mcp) {
        HigressMCPServerResult r = OutputConverter.super.convertFrom(mcp);
        r.setMcpServerName(mcp.getName());
        return r;
    }
}