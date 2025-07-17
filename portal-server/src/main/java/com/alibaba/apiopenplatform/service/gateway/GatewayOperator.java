package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.repository.GatewayRepository;
import com.alibaba.apiopenplatform.service.gateway.client.APIGClient;
import com.alibaba.apiopenplatform.service.gateway.client.GatewayClient;
import com.alibaba.apiopenplatform.service.gateway.client.HigressClient;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zh
 */
@Slf4j
public abstract class GatewayOperator<T> {

    private final Map<String, GatewayClient> clientCache = new ConcurrentHashMap<>();

    abstract public PageResult<APIResult> fetchHTTPAPIs(Gateway gateway, int pageNumber, int pageSize);

    abstract public PageResult<APIResult> fetchRESTAPIs(Gateway gateway, int pageNumber, int pageSize);

    abstract public PageResult<MCPServerResult> fetchMcpServers(Gateway gateway, int pageNumber, int pageSize);

    abstract public void fetchAPISpec(Gateway gateway, String apiId);

    abstract public void createConsumer(Gateway gateway);

    abstract public void deleteConsumer(Gateway gateway);


    abstract public GatewayType getGatewayType();

    @SuppressWarnings("unchecked")
    protected T getClient(Gateway gateway) {
        return (T) clientCache.computeIfAbsent(
                gateway.getGatewayId(),
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
                return new APIGClient(gateway);
            case HIGRESS:
                return new HigressClient(gateway);
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
