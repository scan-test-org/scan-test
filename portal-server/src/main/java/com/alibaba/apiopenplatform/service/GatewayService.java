package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.gateway.ImportGatewayParam;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.GatewayResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.Consumer;
import org.springframework.data.domain.Pageable;

/**
 * @author zh
 */
public interface GatewayService {

    /**
     * 获取APIG Gateway列表
     *
     * @param param
     * @param pageable
     * @return
     */
    PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable);

    /**
     * 导入Gateway
     *
     * @param param
     */
    void importGateway(ImportGatewayParam param);

    /**
     * 获取导入的Gateway列表
     *
     * @param pageable
     * @return
     */
    PageResult<GatewayResult> listGateways(Pageable pageable);

    /**
     * 删除Gateway
     *
     * @param gatewayId
     */
    void deleteGateway(String gatewayId);

    /**
     * 检查API是否已经部署
     *
     * @param gatewayId
     * @param apiId
     * @return
     */
    APIResult fetchAPI(String gatewayId, String apiId);



    /**
     * 拉取网关API列表
     *
     * @param gatewayId
     * @param apiType
     * @param pageable
     * @return
     */
    PageResult<APIResult> fetchAPIs(String gatewayId, String apiType, Pageable pageable);

    PageResult<APIResult> fetchHTTPAPIs(String gatewayId, Pageable pageable);

    PageResult<APIResult> fetchRESTAPIs(String gatewayId, Pageable pageable);

    PageResult<APIResult> fetchRoutes(String gatewayId, Pageable pageable);

    PageResult<MCPServerResult> fetchMcpServers(String gatewayId, Pageable pageable);

    String fetchAPISpec(String gatewayId, String apiId);

    String fetchMcpSpec(String gatewayId, String apiId, String routeId);

    void createConsumer(Consumer consumer);

    void deleteConsumer(Consumer consumer);
}
