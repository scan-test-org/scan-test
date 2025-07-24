package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.service.gateway.client.HigressClient;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author zh
 */
@Service
@Slf4j
public class HigressOperator extends GatewayOperator<HigressClient> {

    @Override
    public PageResult<APIResult> fetchHTTPAPIs(Gateway gateway, Pageable pageable) {
        throw new UnsupportedOperationException("Higress gateway does not support HTTP APIs");
    }

    @Override
    public PageResult<APIResult> fetchRESTAPIs(Gateway gateway, Pageable pageable) {
        throw new UnsupportedOperationException("Higress gateway does not support REST APIs");
    }

    @Override
    public PageResult<MCPServerResult> fetchMcpServers(Gateway gateway, Pageable pageable) {

        return null;
    }

    @Override
    public String fetchAPISpec(Gateway gateway, String apiId) {
        return null;
    }

    @Override
    public String fetchMcpSpec(Gateway gateway, String apiId, String routeId) {
        return "";
    }

    @Override
    public PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable) {
        throw new UnsupportedOperationException("Higress gateway does not support fetching Gateways");
    }

    @Override
    public void createConsumer(Gateway gateway) {
    }

    @Override
    public void deleteConsumer(Gateway gateway) {

    }

    @Override
    public APIResult fetchAPI(Gateway gateway, String apiId) {
        return null;
    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.HIGRESS;
    }
}
