package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.params.gateway.ImportGatewayParam;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.GatewayMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable) {
        return gatewayOperators.get(param.getGatewayType()).fetchGateways(param, pageable);
    }

    public void importGateway(ImportGatewayParam param) {
        gatewayRepository.findByGatewayId(param.getGatewayId())
                .ifPresent(gateway -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.GATEWAY, param.getGatewayId());
                });

        Gateway gateway = param.convertTo();
        gateway.setAdminId("admin");
        gatewayRepository.save(gateway);
    }

    @Override
    public GatewayResult getGateway(String gatewayId) {
        Gateway gateway = findGateway(gatewayId);

        return new GatewayResult().convertFrom(gateway);
    }

    @Override
    public PageResult<GatewayResult> listGateways(Pageable pageable) {
        Page<Gateway> gateways = gatewayRepository.findByAdminId("admin", pageable);

        return new PageResult<GatewayResult>().convertFrom(gateways, gateway -> new GatewayResult().convertFrom(gateway));
    }

    @Override
    public void deleteGateway(String gatewayId) {
        Gateway gateway = findGateway(gatewayId);
        gatewayRepository.delete(gateway);
    }

    @Override
    public PageResult<APIResult> fetchAPIs(String gatewayId, String apiType, Pageable pageable) {
        Gateway gateway = findGateway(gatewayId);
        GatewayType gatewayType = gateway.getGatewayType();

        if (gatewayType.isAPIG()) {
            APIGAPIType type = EnumUtil.fromString(APIGAPIType.class, apiType);
            switch (type) {
                case REST:
                    return fetchRESTAPIs(gatewayId, pageable);
                case HTTP:
                    return fetchHTTPAPIs(gatewayId, pageable);
                default:
            }
        }

        if (gatewayType.isHigress()) {
            return fetchRoutes(gatewayId, pageable);
        }

        throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                String.format("Gateway type %s does not support API type %s", gatewayType, apiType));
    }

    @Override
    public PageResult<APIResult> fetchHTTPAPIs(String gatewayId, Pageable pageable) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchHTTPAPIs(gateway, pageable);
    }

    @Override
    public PageResult<APIResult> fetchRESTAPIs(String gatewayId, Pageable pageable) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchRESTAPIs(gateway, pageable);
    }

    @Override
    public PageResult<APIResult> fetchRoutes(String gatewayId, Pageable pageable) {
        return null;
    }

    @Override
    public PageResult<GatewayMCPServerResult> fetchMcpServers(String gatewayId, Pageable pageable) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchMcpServers(gateway, pageable);
    }

    @Override
    public String fetchAPIConfig(String gatewayId, Object config) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchAPIConfig(gateway, config);
    }

    @Override
    public String fetchMcpConfig(String gatewayId, Object conf) {
        Gateway gateway = findGateway(gatewayId);
        return getOperator(gateway).fetchMcpConfig(gateway, conf);
    }

    @Override
    public String createConsumer(Consumer consumer) {
        List<Gateway> gateways = findAllGateways();
        // 由于可能有多个网关，我们返回第一个网关的Consumer ID
        // 在实际使用中，应该根据具体的网关类型来决定返回哪个ID
        String gwConsumerId = null;
        for (Gateway gateway : gateways) {
            try {
                gwConsumerId = getOperator(gateway).createConsumer(gateway, consumer);
                if (gwConsumerId != null) {
                    break; // 使用第一个成功创建的Consumer ID
                }
            } catch (Exception e) {
                log.error("Failed to create consumer in gateway {}", gateway.getGatewayId(), e);
                // 继续尝试其他网关
            }
        }
        return gwConsumerId != null ? gwConsumerId : consumer.getConsumerId(); // 如果都失败了，返回原始Consumer ID
    }

    @Override
    public void deleteConsumer(Consumer consumer) {
        List<Gateway> gateways = findAllGateways();
        for (Gateway gateway : gateways) {
            getOperator(gateway).deleteConsumer(gateway);
        }
    }

    @Override
    public void authorizationConsumerToApi(Consumer consumer, String apiId) {
        List<Gateway> gateways = findAllGateways();
        for (Gateway gateway : gateways) {
            getOperator(gateway).authorizationConsumerToApi(gateway, consumer.getConsumerId(), apiId);
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
        return gatewayRepository.findByGatewayId(gatewayId)
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