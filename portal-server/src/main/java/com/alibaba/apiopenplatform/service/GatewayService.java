package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.Consumer;

/**
 * @author zh
 */
public interface GatewayService {

    PageResult<APIResult> fetchAPIs(String gatewayId, String apiType, int pageNumber, int pageSize);

    PageResult<APIResult> fetchHTTPAPIs(String gatewayId, int pageNumber, int pageSize);

    PageResult<APIResult> fetchRESTAPIs(String gatewayId, int pageNumber, int pageSize);

    PageResult<APIResult> fetchRoutes(String gatewayId, int pageNumber, int pageSize);

    PageResult<MCPServerResult> fetchMcpServers(String gatewayId, int pageNumber, int pageSize);

    void fetchAPISpec(String gatewayId, String apiId);

    void createConsumer(Consumer consumer);

    void deleteConsumer(Consumer consumer);
}
