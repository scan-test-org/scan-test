package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.service.gateway.client.HigressClient;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zh
 */
@Service
@Slf4j
public class HigressOperator extends GatewayOperator<HigressClient> {

    @Override
    public PageResult<APIResult> fetchHTTPAPIs(Gateway gateway, int pageNumber, int pageSize) {
        throw new UnsupportedOperationException("Higress gateway does not support HTTP APIs");
    }

    @Override
    public PageResult<APIResult> fetchRESTAPIs(Gateway gateway, int pageNumber, int pageSize) {
        throw new UnsupportedOperationException("Higress gateway does not support REST APIs");
    }

    @Override
    public PageResult<MCPServerResult> fetchMcpServers(Gateway gateway, int pageNumber, int pageSize) {

        return null;
    }

    @Override
    public void fetchAPISpec(Gateway gateway, String apiId) {

    }

    @Override
    public void createConsumer(Gateway gateway) {
    }

    @Override
    public void deleteConsumer(Gateway gateway) {

    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.HIGRESS;
    }
}
