package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.params.gateway.ImportGatewayParam;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.*;
import com.alibaba.apiopenplatform.repository.ConsumerCredentialRepository;
import com.alibaba.apiopenplatform.repository.ConsumerRefRepository;
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
import java.util.Optional;
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

    private ConsumerRefRepository consumerRefRepository;

    private ConsumerCredentialRepository consumerCredentialRepository;

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
    public String createConsumer(String gatewayId, Consumer consumer, ConsumerCredential credential) {
        if (gatewayId == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Gateway cannot be null");
        }
        if (consumer == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Consumer cannot be null");
        }
        if (credential == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Credential cannot be null");
        }
        Gateway gateway = findGateway(gatewayId);
        String gwConsumerId = getOperator(gateway).createConsumer(gateway, consumer, credential);

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
    public void authorizationConsumerToApi(Consumer consumer, ProductRef productRef) {
        List<Gateway> gateways = findAllGateways();
        for (Gateway gateway : gateways) {
            getOperator(gateway).authorizationConsumerToApi(gateway, consumer.getConsumerId(), productRef);
        }
    }

    @Override
    public void assertGatewayConsumerExist(String gatewayId, Consumer consumer) {
        Gateway gateway = gatewayRepository.findByGatewayId(gatewayId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Gateway", gatewayId));

        // todo 需要放到 GatewayOperator
        Optional<ConsumerRef> existingRef = consumerRefRepository.findByConsumerIdAndRegionAndGatewayType(consumer.getConsumerId(), gateway.getApigConfig().getRegion(), gateway.getGatewayType().name());

        if (!existingRef.isPresent()) {
            // 创建网关Consumer 需要拉到 Consumer 关联的 Credential
            ConsumerCredential credential = consumerCredentialRepository.findByConsumerId(consumer.getConsumerId()).orElse(null);
            createGatewayConsumer(consumer, gateway, credential);
        }
    }

    private void createGatewayConsumer(Consumer consumer, Gateway gateway, ConsumerCredential credential) {
        try {
            // 调用网关创建Consumer，获取返回的网关Consumer ID
            String gwConsumerId = createConsumer(gateway.getGatewayId(), consumer, credential);

            // 创建映射关系
            ConsumerRef consumerRef = new ConsumerRef();
            consumerRef.setConsumerId(consumer.getConsumerId());
            consumerRef.setRegion(gateway.getGatewayId()); // 使用gatewayId作为region
            consumerRef.setGatewayType(gateway.getGatewayType().name());
            consumerRef.setGwConsumerId(gwConsumerId); // 使用网关返回的真实Consumer ID

            consumerRefRepository.save(consumerRef);

            log.info("Created gateway consumer {} for consumer {} in gateway {}", gwConsumerId, consumer.getConsumerId(), gateway.getGatewayId());
        } catch (Exception e) {
            log.error("Failed to create consumer in gateway {}", gateway.getGatewayId(), e);
            throw e;
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