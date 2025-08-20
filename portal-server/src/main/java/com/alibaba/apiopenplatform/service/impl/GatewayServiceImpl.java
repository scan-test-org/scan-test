package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.gateway.ImportGatewayParam;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.*;
import com.alibaba.apiopenplatform.repository.ConsumerCredentialRepository;
import com.alibaba.apiopenplatform.repository.ConsumerRefRepository;
import com.alibaba.apiopenplatform.repository.GatewayRepository;
import com.alibaba.apiopenplatform.service.GatewayService;
import com.alibaba.apiopenplatform.service.gateway.GatewayOperator;
import com.alibaba.apiopenplatform.support.consumer.ConsumerAuthConfig;
import com.alibaba.apiopenplatform.support.enums.APIGAPIType;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.gateway.GatewayConfig;
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

    private final ContextHolder contextHolder;

    public PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable) {
        return gatewayOperators.get(param.getGatewayType()).fetchGateways(param, pageable);
    }

    public void importGateway(ImportGatewayParam param) {
        gatewayRepository.findByGatewayId(param.getGatewayId())
                .ifPresent(gateway -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.GATEWAY, param.getGatewayId());
                });

        Gateway gateway = param.convertTo();
        if (gateway.getGatewayType().isHigress()) {
            gateway.setGatewayId(IdGenerator.genHigressGatewayId());
        }
        gateway.setAdminId(contextHolder.getUser());
        gatewayRepository.save(gateway);
    }

    @Override
    public GatewayResult getGateway(String gatewayId) {
        Gateway gateway = findGateway(gatewayId);

        return new GatewayResult().convertFrom(gateway);
    }

    @Override
    public PageResult<GatewayResult> listGateways(Pageable pageable) {
        Page<Gateway> gateways = gatewayRepository.findAll(pageable);

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
    public String createConsumer(Consumer consumer, ConsumerCredential credential, GatewayConfig config) {
       return gatewayOperators.get(config.getGatewayType()).createConsumer(consumer, credential, config);
    }

    @Override
    public void updateConsumer(String gwConsumerId, ConsumerCredential credential, GatewayConfig config) {
        gatewayOperators.get(config.getGatewayType()).updateConsumer(gwConsumerId, credential, config);
    }

    @Override
    public void deleteConsumer(String gwConsumerId, GatewayConfig config) {
        gatewayOperators.get(config.getGatewayType()).deleteConsumer(gwConsumerId, config);
    }

    @Override
    public ConsumerAuthConfig authorizeConsumer(String gatewayId, String gwConsumerId, ProductRefResult productRef) {
        Gateway gateway = findGateway(gatewayId);
        Object refConfig = gateway.getGatewayType().isHigress() ?
                productRef.getHigressRefConfig() : productRef.getApigRefConfig();
        return getOperator(gateway).authorizeConsumer(gateway, gwConsumerId, refConfig);
    }

    @Override
    public void deathAuthorizeConsumer(String gatewayId, String gwConsumerId, ConsumerAuthConfig config) {
        Gateway gateway = findGateway(gatewayId);

        getOperator(gateway).revokeConsumerAuthorization(gateway, gwConsumerId, config);
    }

    @Override
    public GatewayConfig getGatewayConfig(String gatewayId) {
        Gateway gateway = findGateway(gatewayId);

        return GatewayConfig.builder()
                .gatewayType(gateway.getGatewayType())
                .apigConfig(gateway.getApigConfig())
                .higressConfig(gateway.getHigressConfig())
                .build();
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

    private GatewayOperator getOperator(Gateway gateway) {
        GatewayOperator gatewayOperator = gatewayOperators.get(gateway.getGatewayType());
        if (gatewayOperator == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "No gateway operator found for gateway type: " + gateway.getGatewayType());
        }
        return gatewayOperator;
    }
}