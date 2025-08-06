package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.event.DeveloperDeletingEvent;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.consumer.QueryConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateCredentialParam;
import com.alibaba.apiopenplatform.dto.params.consumer.UpdateCredentialParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.dto.result.ConsumerCredentialResult;
import com.alibaba.apiopenplatform.dto.result.SubscriptionResult;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateSubscriptionParam;
import com.alibaba.apiopenplatform.dto.params.consumer.QuerySubscriptionParam;
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.entity.ConsumerCredential;
import com.alibaba.apiopenplatform.entity.ProductSubscription;
import com.alibaba.apiopenplatform.repository.ConsumerRepository;
import com.alibaba.apiopenplatform.repository.ConsumerCredentialRepository;
import com.alibaba.apiopenplatform.repository.SubscriptionRepository;
import com.alibaba.apiopenplatform.service.ConsumerService;
import com.alibaba.apiopenplatform.service.GatewayService;
import com.alibaba.apiopenplatform.service.PortalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.apiopenplatform.support.enums.SubscriptionStatus;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.entity.Product;
import com.alibaba.apiopenplatform.entity.ProductRef;
import com.alibaba.apiopenplatform.entity.ConsumerRef;
import com.alibaba.apiopenplatform.repository.GatewayRepository;
import com.alibaba.apiopenplatform.repository.ProductRepository;
import com.alibaba.apiopenplatform.repository.ProductRefRepository;
import com.alibaba.apiopenplatform.repository.ConsumerRefRepository;
import java.util.Optional;

/**
 * @author zh
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {

    private final PortalService portalService;

    private final ConsumerRepository consumerRepository;

    private final GatewayService gatewayService;

    private final ContextHolder contextHolder;

    private final ConsumerCredentialRepository consumerCredentialRepository;

    private final SubscriptionRepository subscriptionRepository;

    private final ProductRepository productRepository;

    private final ProductRefRepository productRefRepository;

    private final GatewayRepository gatewayRepository;

    private final ConsumerRefRepository consumerRefRepository;

    @Override
    public ConsumerResult createConsumer(CreateConsumerParam param) {
        PortalResult portal = portalService.getPortal(contextHolder.getPortal());

        String consumerId = IdGenerator.genConsumerId();
        Consumer consumer = param.convertTo();
        consumer.setConsumerId(consumerId);
        consumer.setDeveloperId(contextHolder.getUser());
        consumer.setPortalId(portal.getPortalId());

        consumerRepository.save(consumer);
        return getConsumer(consumerId);
    }

    @Override
    public PageResult<ConsumerResult> listConsumers(QueryConsumerParam param, Pageable pageable) {
        Page<Consumer> consumers = consumerRepository.findAll(buildSpecification(param), pageable);

        return new PageResult<ConsumerResult>().convertFrom(consumers, consumer -> new ConsumerResult().convertFrom(consumer));
    }

    @Override
    public ConsumerResult getConsumer(String consumerId) {
        Consumer consumer = contextHolder.isDeveloper() ? findDevConsumer(consumerId) : findConsumer(consumerId);

        return new ConsumerResult().convertFrom(consumer);
    }

    @Override
    public void deleteConsumer(String consumerId) {
        Consumer consumer = contextHolder.isDeveloper() ? findDevConsumer(consumerId) : findConsumer(consumerId);
        
        // 删除订阅关系
        List<ProductSubscription> subscriptions = subscriptionRepository.findAll((root, query, cb) -> 
            cb.equal(root.get("consumerId"), consumerId));
        subscriptionRepository.deleteAll(subscriptions);
        
        // 删除凭证
        consumerCredentialRepository.findByConsumerId(consumerId).ifPresent(consumerCredentialRepository::delete);
        
        // 删除网关Consumer映射关系
        List<ConsumerRef> consumerRefs = consumerRefRepository.findByConsumerId(consumerId);
        consumerRefRepository.deleteAll(consumerRefs);
        
        // 删除本地Consumer
        consumerRepository.delete(consumer);
    }

    @Override
    public ConsumerCredentialResult createCredential(String consumerId, CreateCredentialParam param) {
        // 校验consumer存在
        if (contextHolder.isDeveloper()) {
            findDevConsumer(consumerId);
        } else {
            findConsumer(consumerId);
        }
        // 检查是否已存在凭证
        consumerCredentialRepository.findByConsumerId(consumerId).ifPresent(c -> {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, "ConsumerCredential", consumerId);
        });
        ConsumerCredential credential = new ConsumerCredential();
        credential.setConsumerId(consumerId);
        credential.setApiKeyConfig(param.getApiKeyConfig());
        credential.setHmacConfig(param.getHmacConfig());
        credential.setJwtConfig(param.getJwtConfig());
        consumerCredentialRepository.save(credential);
        ConsumerCredentialResult result = new ConsumerCredentialResult();
        result.setApiKeyConfig(credential.getApiKeyConfig());
        result.setHmacConfig(credential.getHmacConfig());
        result.setJwtConfig(credential.getJwtConfig());
        return result;
    }

    @Override
    public ConsumerCredentialResult getCredential(String consumerId) {
        ConsumerCredential credential = consumerCredentialRepository.findByConsumerId(consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Consumer credential not found"));
        ConsumerCredentialResult result = new ConsumerCredentialResult();
        result.setApiKeyConfig(credential.getApiKeyConfig());
        result.setHmacConfig(credential.getHmacConfig());
        result.setJwtConfig(credential.getJwtConfig());
        return result;
    }

    @Override
    public ConsumerCredentialResult updateCredential(String consumerId, UpdateCredentialParam param) {
        ConsumerCredential credential = consumerCredentialRepository.findByConsumerId(consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Consumer credential not found"));
        if (param.getApiKeyConfig() != null) {
            credential.setApiKeyConfig(param.getApiKeyConfig());
        }
        if (param.getHmacConfig() != null) {
            credential.setHmacConfig(param.getHmacConfig());
        }
        if (param.getJwtConfig() != null) {
            credential.setJwtConfig(param.getJwtConfig());
        }
        consumerCredentialRepository.save(credential);
        ConsumerCredentialResult result = new ConsumerCredentialResult();
        result.setApiKeyConfig(credential.getApiKeyConfig());
        result.setHmacConfig(credential.getHmacConfig());
        result.setJwtConfig(credential.getJwtConfig());
        return result;
    }

    @Override
    public void deleteCredential(String consumerId) {
        ConsumerCredential credential = consumerCredentialRepository.findByConsumerId(consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Consumer credential not found"));
        consumerCredentialRepository.delete(credential);
    }

    @Override
    public SubscriptionResult subscribeProduct(String consumerId, CreateSubscriptionParam param) {
        // 校验consumer存在
        Consumer consumer = contextHolder.isDeveloper() ? findDevConsumer(consumerId) : findConsumer(consumerId);
        
        // 检查是否已订阅
        subscriptionRepository.findOne((root, query, cb) -> cb.and(
                cb.equal(root.get("consumerId"), consumerId),
                cb.equal(root.get("productId"), param.getProductId())
        )).ifPresent(s -> {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, "ProductSubscription", consumerId + ":" + param.getProductId());
        });
        
        // 获取产品信息，找到对应的API ID
        Product product = productRepository.findByProductId(param.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Product", param.getProductId()));
        
        // 获取产品的API引用信息
        ProductRef productRef = productRefRepository.findFirstByProductId(param.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_API_NOT_FOUND, param.getProductId()));
        
        // 为每个网关处理Consumer创建和授权
        try {
            Gateway gateway = gatewayRepository.findByGatewayId(productRef.getGatewayId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Gateway", productRef.getGatewayId()));
            // 检查是否已存在该网关的Consumer映射
            Optional<ConsumerRef> existingRef = consumerRefRepository.findByConsumerIdAndUidAndRegionAndGatewayType(consumerId, gateway.getApigConfig().getUid(), gateway.getApigConfig().getRegion(), gateway.getGatewayType().name());

            if (!existingRef.isPresent()) {
                // 创建网关Consumer
                createGatewayConsumer(consumer, gateway);
            }

            // 获取API ID并创建授权关系
            String apiId = getApiIdFromProductRef(productRef, gateway);
            if (apiId != null) {
                // 获取网关Consumer ID
                ConsumerRef consumerRef = consumerRefRepository.findByConsumerIdAndUidAndRegionAndGatewayType(consumerId, gateway.getApigConfig().getUid(), gateway.getApigConfig().getRegion(), gateway.getGatewayType().name())
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ConsumerRef", consumerId + ":" + gateway.getGatewayType()));

                // 创建授权关系
                gatewayService.authorizationConsumerToApi(consumer, apiId);

                log.info("Authorized consumer {} to apiId {} in gateway {}", consumerId, apiId, gateway.getGatewayId());
            }
        } catch (Exception e) {
            log.error("Failed to process consumer {} for product {} in gateway {}", consumerId, param.getProductId(), productRef.getGatewayId(), e);
            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "Failed to process consumer in gateway: " + e.getMessage());
        }
        
        // 创建订阅记录
        ProductSubscription subscription = new ProductSubscription();
        subscription.setConsumerId(consumerId);
        subscription.setProductId(param.getProductId());
        subscription.setDeveloperId(consumer.getDeveloperId());
        subscription.setPortalId(consumer.getPortalId());
        subscription.setStatus(SubscriptionStatus.APPROVED);
        subscriptionRepository.save(subscription);
        
        return new SubscriptionResult().convertFrom(subscription);
    }

    @Override
    public PageResult<SubscriptionResult> listSubscriptions(String consumerId, QuerySubscriptionParam param, Pageable pageable) {
        // 校验consumer存在
        if (contextHolder.isDeveloper()) {
            findDevConsumer(consumerId);
        } else {
            findConsumer(consumerId);
        }
        Specification<ProductSubscription> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("consumerId"), consumerId));
            if (param.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), param.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ProductSubscription> page = subscriptionRepository.findAll(spec, pageable);
        return new PageResult<SubscriptionResult>().convertFrom(page, s -> new SubscriptionResult().convertFrom(s));
    }

    @Override
    public void deleteSubscription(String consumerId, String productId) {
        // 校验consumer存在
        if (contextHolder.isDeveloper()) {
            findDevConsumer(consumerId);
        } else {
            findConsumer(consumerId);
        }
        Specification<ProductSubscription> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("consumerId"), consumerId),
                cb.equal(root.get("productId"), productId)
        );
        ProductSubscription subscription = subscriptionRepository.findOne(spec)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "ProductSubscription", consumerId + ":" + productId));
        subscriptionRepository.delete(subscription);
    }

    private Consumer findConsumer(String consumerId) {
        return consumerRepository.findByConsumerId(consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    private Consumer findDevConsumer(String consumerId) {
        return consumerRepository.findByDeveloperIdAndConsumerId(contextHolder.getUser(), consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    public Specification<Consumer> buildSpecification(QueryConsumerParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (contextHolder.isDeveloper()) {
                param.setDeveloperId(contextHolder.getUser());
            }

            if (StrUtil.isNotBlank(param.getDeveloperId())) {
                predicates.add(cb.equal(root.get("developerId"), param.getDeveloperId()));
            }

            if (StrUtil.isNotBlank(param.getPortalId())) {
                predicates.add(cb.equal(root.get("portalId"), param.getPortalId()));
            }

            if (StrUtil.isNotBlank(param.getName())) {
                String likePattern = "%" + param.getName() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @EventListener
    @Async("taskExecutor")
    public void handleDeveloperDeletion(DeveloperDeletingEvent event) {
        String developerId = event.getDeveloperId();
        try {
            log.info("Cleaning consumers for developer {}", developerId);

            List<Consumer> consumers = consumerRepository.findAllByDeveloperId(developerId);
            consumers.forEach(consumer -> {
                gatewayService.deleteConsumer(consumer);
                consumerRepository.delete(consumer);
                // TODO 清除凭证
            });
        } catch (Exception e) {
            log.error("Failed to clean consumers for developer {}", developerId, e);
        }
    }

    private void createGatewayConsumer(Consumer consumer, Gateway gateway) {
        try {
            // 调用网关创建Consumer，获取返回的网关Consumer ID
            String gwConsumerId = gatewayService.createConsumer(consumer);
            
            // 创建映射关系
            ConsumerRef consumerRef = new ConsumerRef();
            consumerRef.setConsumerId(consumer.getConsumerId());
            consumerRef.setUid(consumer.getDeveloperId()); // 使用developerId作为uid
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

    private String getApiIdFromProductRef(ProductRef productRef, Gateway gateway) {
        // 根据网关类型从ProductRef中获取API ID
        switch (gateway.getGatewayType()) {
            case APIG_API:
            case APIG_AI:
                return productRef.getApigRefConfig() != null ? productRef.getApigRefConfig().getApiId() : null;
            case HIGRESS:
                return productRef.getHigressRefConfig() != null ? productRef.getHigressRefConfig().getMcpServerName() : null;
            default:
                return null;
        }
    }
}
