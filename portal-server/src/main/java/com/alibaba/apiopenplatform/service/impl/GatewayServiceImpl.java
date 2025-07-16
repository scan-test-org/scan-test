package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.repository.GatewayRepository;
import com.alibaba.apiopenplatform.service.GatewayService;
import com.alibaba.apiopenplatform.service.gateway.GatewayOperator;
import com.alibaba.apiopenplatform.support.enums.APIGAPIType;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
@Slf4j
public class GatewayServiceImpl implements GatewayService, ApplicationContextAware {

    private final GatewayRepository gatewayRepository;

    private Map<GatewayType, GatewayOperator> gatewayOperators;

    @Override
    public PageResult<APIResult> fetchAPIs(String gatewayId, String apiType, int pageNumber, int pageSize) {
        Gateway gateway = findGateway(gatewayId);
        GatewayType gatewayType = gateway.getGatewayType();

        if (gatewayType.isAPIG()) {
            APIGAPIType type = EnumUtil.fromString(APIGAPIType.class, apiType);
            switch (type) {
                case REST:
                    return fetchRESTAPIs(gatewayId, pageNumber, pageSize);
                case MCP:
                    return fetchHTTPAPIs(gatewayId, pageNumber, pageSize);
                default:
            }
        }

        if (gatewayType.isHigress()) {
            return fetchRoutes(gatewayId, pageNumber, pageSize);
        }

        throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                String.format("Gateway type %s does not support API type %s", gatewayType, apiType));
    }

    @Override
    public PageResult<APIResult> fetchHTTPAPIs(String gatewayId, int pageNumber, int pageSize) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchHTTPAPIs(gateway, pageNumber, pageSize);
    }

    @Override
    public PageResult<APIResult> fetchRESTAPIs(String gatewayId, int pageNumber, int pageSize) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchRESTAPIs(gateway, pageNumber, pageSize);
    }

    @Override
    public PageResult<APIResult> fetchRoutes(String gatewayId, int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public PageResult<MCPServerResult> fetchMcpServers(String gatewayId, int pageNumber, int pageSize) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchMcpServers(gateway, pageNumber, pageSize);
    }

    @Override
    public void fetchAPISpec(String gatewayId, String apiId) {
        Gateway gateway = findGateway(gatewayId);
//        return getOperator(gateway).fetchHTTPAPIs(gateway, pageNumber, pageSize);
    }

    @Override
    public void createConsumer(Consumer consumer) {
        List<Gateway> gateways = findAllGateways();
        for (Gateway gateway : gateways) {
            getOperator(gateway).createConsumer(gateway);
        }
    }

    @Override
    public void deleteConsumer(Consumer consumer) {
        List<Gateway> gateways = findAllGateways();
        for (Gateway gateway : gateways) {
            getOperator(gateway).deleteConsumer(gateway);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, GatewayOperator> operators = applicationContext.getBeansOfType(GatewayOperator.class);

        gatewayOperators = operators.values().stream()
                .collect(Collectors.toMap(
                        operator -> operator.getGatewayType(),
                        operator -> operator,
                        (existing, replacement) -> existing));
    }

    private Gateway findGateway(String gatewayId) {
        return gatewayRepository.findByAdminIdAndGatewayId("admin", gatewayId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.GATEWAY, gatewayId));
    }

    private List<Gateway> findAllGateways() {
        List<Gateway> gateways = gatewayRepository.findAll();
        if (gateways.isEmpty()) {
            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "No gateways found");
        }
        return gateways;
    }

    private GatewayOperator getOperator(Gateway gateway) {
        GatewayOperator gatewayOperator = gatewayOperators.get(gateway.getGatewayType());
        if (gatewayOperator == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "No gateway operator found for gateway type: " + gateway.getGatewayType());
        }
        return gatewayOperator;
    }
}
