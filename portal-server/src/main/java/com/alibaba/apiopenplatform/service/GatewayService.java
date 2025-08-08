package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.gateway.ImportGatewayParam;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.GatewayMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.entity.ConsumerCredential;
import com.alibaba.apiopenplatform.entity.ProductRef;
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


    GatewayResult getGateway(String gatewayId);

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

    PageResult<GatewayMCPServerResult> fetchMcpServers(String gatewayId, Pageable pageable);

    String fetchAPIConfig(String gatewayId, Object config);

    String fetchMcpConfig(String gatewayId, Object conf);

    String createConsumer(String gatewayId, Consumer consumer, ConsumerCredential credential);

    void deleteConsumer(Consumer consumer);

    /**
     * 为消费者授权访问指定的API
     * @param consumer 消费者
     * @param apiId API ID
     */
    void authorizationConsumerToApi(Consumer consumer, ProductRef productRef);

    void assertGatewayConsumerExist(String gatewayId, Consumer consumer);
}
