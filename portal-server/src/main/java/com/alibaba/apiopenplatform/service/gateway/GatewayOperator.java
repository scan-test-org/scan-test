package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.service.gateway.client.APIGClient;
import com.alibaba.apiopenplatform.service.gateway.client.GatewayClient;
import com.alibaba.apiopenplatform.service.gateway.client.HigressClient;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zh
 */
@Slf4j
public abstract class GatewayOperator<T> {

    private final Map<String, GatewayClient> clientCache = new ConcurrentHashMap<>();

    abstract public PageResult<APIResult> fetchHTTPAPIs(Gateway gateway, Pageable pageable);

    abstract public PageResult<APIResult> fetchRESTAPIs(Gateway gateway, Pageable pageable);

    abstract public PageResult<? extends MCPServerResult> fetchMcpServers(Gateway gateway, Pageable pageable);

    abstract public String fetchAPISpec(Gateway gateway, Object config);

    abstract public String fetchMcpSpec(Gateway gateway, Object conf);

    abstract public PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable);

    abstract public void createConsumer(Gateway gateway);

    abstract public void deleteConsumer(Gateway gateway);

    abstract public APIResult fetchAPI(Gateway gateway, String apiId);

    abstract public PageResult<PluginAttachmentResult> fetchPluginAttachment(Gateway gateway, String resourceType, String resourceId, Pageable pageable);

    abstract public GatewayType getGatewayType();

    @SuppressWarnings("unchecked")
    protected T getClient(Gateway gateway) {
        String clientKey = gateway.getGatewayType().isAPIG() ?
                gateway.getApigConfig().buildUniqueKey() : gateway.getHigressConfig().buildUniqueKey();
        return (T) clientCache.computeIfAbsent(
                clientKey,
                key -> createClient(gateway)
        );
    }

    /**
     * 创建网关客户端
     */
    private GatewayClient createClient(Gateway gateway) {
        switch (gateway.getGatewayType()) {
            case APIG_API:
            case APIG_AI:
                return new APIGClient(gateway.getApigConfig());
            case HIGRESS:
                return new HigressClient(gateway.getHigressConfig());
            default:
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "No factory found for gateway type: " + gateway.getGatewayType());
        }
    }

    /**
     * 移除网关客户端
     */
    public void removeClient(String instanceId) {
        GatewayClient client = clientCache.remove(instanceId);
        try {
            client.close();
        } catch (Exception e) {
            log.error("Error closing client for instance: {}", instanceId, e);
        }
    }
}
