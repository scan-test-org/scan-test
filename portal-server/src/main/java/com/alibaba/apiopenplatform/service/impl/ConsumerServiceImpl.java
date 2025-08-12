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
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateSubscriptionParam;
import com.alibaba.apiopenplatform.dto.params.consumer.QuerySubscriptionParam;
import com.alibaba.apiopenplatform.entity.*;
import com.alibaba.apiopenplatform.repository.ConsumerRepository;
import com.alibaba.apiopenplatform.repository.ConsumerCredentialRepository;
import com.alibaba.apiopenplatform.repository.SubscriptionRepository;
import com.alibaba.apiopenplatform.service.ConsumerService;
import com.alibaba.apiopenplatform.service.GatewayService;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.service.ProductService;
import com.alibaba.apiopenplatform.support.consumer.ApiKeyConfig;
import com.alibaba.apiopenplatform.support.consumer.HmacConfig;
import com.alibaba.apiopenplatform.support.enums.CredentialMode;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import com.alibaba.apiopenplatform.support.gateway.GatewayIdentityConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.apiopenplatform.support.enums.SubscriptionStatus;
import com.alibaba.apiopenplatform.repository.GatewayRepository;
import com.alibaba.apiopenplatform.repository.ProductRefRepository;
import com.alibaba.apiopenplatform.repository.ConsumerRefRepository;

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

    private final ConsumerCredentialRepository credentialRepository;

    private final SubscriptionRepository subscriptionRepository;

    private final ProductService productService;

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
        Page<Consumer> consumers = consumerRepository.findAll(buildConsumerSpec(param), pageable);

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
        // 订阅
        subscriptionRepository.deleteAllByConsumerId(consumerId);

        // 凭证
        credentialRepository.deleteAllByConsumerId(consumerId);

//        // 删除网关Consumer映射关系
//        List<ConsumerRef> consumerRefs = consumerRefRepository.findByConsumerId(consumerId);
//        consumerRefRepository.deleteAll(consumerRefs);

        consumerRepository.delete(consumer);
    }

    @Override
    public void createCredential(String consumerId, CreateCredentialParam param) {
        existsConsumer(consumerId);
        // Consumer仅一份Credential
        credentialRepository.findByConsumerId(consumerId)
                .ifPresent(c -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.CONSUMER_CREDENTIAL, consumerId);
                });
        ConsumerCredential credential = param.convertTo();
        credential.setConsumerId(consumerId);
        complementCredentials(credential);
        credentialRepository.save(credential);
    }

    @Override
    public ConsumerCredentialResult getCredential(String consumerId) {
        existsConsumer(consumerId);

        return credentialRepository.findByConsumerId(consumerId)
                .map(credential -> new ConsumerCredentialResult().convertFrom(credential))
                .orElse(new ConsumerCredentialResult());
    }

    @Override
    public void updateCredential(String consumerId, UpdateCredentialParam param) {
        ConsumerCredential credential = credentialRepository.findByConsumerId(consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER_CREDENTIAL, consumerId));

        param.update(credential);
        credentialRepository.saveAndFlush(credential);
    }

    @Override
    public void deleteCredential(String consumerId) {
        existsConsumer(consumerId);
        credentialRepository.deleteAllByConsumerId(consumerId);
    }

    @Override
    public void subscribeProduct(String consumerId, CreateSubscriptionParam param) {
        Consumer consumer = contextHolder.isDeveloper() ?
                findDevConsumer(consumerId) : findConsumer(consumerId);
        // 勿重复订阅
        if (subscriptionRepository.findByConsumerIdAndProductId(consumerId, param.getProductId()).isPresent()) {
            return;
        }

        ProductRefResult productRef = productService.getProductRef(param.getProductId());
        if (productRef == null) {
            throw new BusinessException(ErrorCode.PRODUCT_API_NOT_FOUND, param.getProductId());
        }

        // 非网关型不支持订阅
        if (productRef.getSourceType() != SourceType.GATEWAY) {
            throw new BusinessException(ErrorCode.PRODUCT_TYPE_NOT_MATCH, param.getProductId());
        }

//        ConsumerCredential credential = credentialRepository.findByConsumerId(consumerId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER_CREDENTIAL, consumerId));
//
//        GatewayIdentityConfig gatewayIdentity = gatewayService.getGatewayIdentity(productRef.getGatewayId());
//
//
//        if (!consumerRefRepository.findConsumerRef(consumerId, gatewayIdentity.getGatewayType(), gatewayIdentity.getIdentity()).isPresent()) {
//            String gwConsumerId = gatewayService.createConsumer(productRef.getGatewayId(), consumer, credential);
//            consumerRefRepository.save(ConsumerRef.builder()
//                    .consumerId(consumerId)
//                    .gwConsumerId(gwConsumerId)
//                    .gatewayType(gatewayIdentity.getGatewayType())
//                    .gatewayIdentity(gatewayIdentity.getIdentity())
//                    .build());
//        }


        // 获取产品的API引用信息
//        ProductRef productRef = productRefRepository.findFirstByProductId(param.getProductId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_API_NOT_FOUND, param.getProductId()));
//
//        // 为每个网关处理Consumer创建和授权
//        try {
//            // 检查是否已存在该网关的Consumer映射
//            gatewayService.assertGatewayConsumerExist(productRef.getGatewayId(), consumer);
//
//            Gateway gateway = gatewayRepository.findByGatewayId(productRef.getGatewayId())
//                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Gateway", productRef.getGatewayId()));
//
//            // 获取API ID并创建授权关系
//            String apiId = getApiIdFromProductRef(productRef, gateway);
//            if (apiId != null) {
//                // 创建授权关系
//                gatewayService.authorizationConsumerToApi(consumer, productRef);
//
//                log.info("Authorized consumer {} to apiId {} in gateway {}", consumerId, apiId, gateway.getGatewayId());
//            }
//        } catch (Exception e) {
//            log.error("Failed to process consumer {} for product {} in gateway {}", consumerId, param.getProductId(), productRef.getGatewayId(), e);
//            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "Failed to process consumer in gateway: " + e.getMessage());
//        }

        // 创建订阅记录
        ProductSubscription subscription = param.convertTo();
        subscription.setConsumerId(consumerId);
        subscription.setStatus(SubscriptionStatus.APPROVED);
        subscriptionRepository.save(subscription);
    }

    @Override
    public PageResult<SubscriptionResult> listSubscriptions(String consumerId, QuerySubscriptionParam param, Pageable pageable) {
        existsConsumer(consumerId);

        Page<ProductSubscription> subscriptions = subscriptionRepository.findAll(buildCredentialSpec(consumerId, param), pageable);

        List<String> productIds = subscriptions.getContent().stream()
                .map(ProductSubscription::getProductId)
                .collect(Collectors.toList());
        Map<String, ProductResult> products = productService.getProducts(productIds);
        return new PageResult<SubscriptionResult>().convertFrom(subscriptions, s -> {
            SubscriptionResult r = new SubscriptionResult().convertFrom(s);
            ProductResult product = products.get(r.getProductId());
            if (product != null) {
                r.setProductType(product.getType());
                r.setProductName(product.getName());
            }
            return r;
        });
    }

    @Override
    public void deleteSubscription(String consumerId, String productId) {
        existsConsumer(consumerId);

        subscriptionRepository.findByConsumerIdAndProductId(consumerId, productId)
                .ifPresent(subscriptionRepository::delete);
    }

    private Consumer findConsumer(String consumerId) {
        return consumerRepository.findByConsumerId(consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    private Consumer findDevConsumer(String consumerId) {
        return consumerRepository.findByDeveloperIdAndConsumerId(contextHolder.getUser(), consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    private void existsConsumer(String consumerId) {
        (contextHolder.isDeveloper() ?
                consumerRepository.findByDeveloperIdAndConsumerId(contextHolder.getUser(), consumerId) :
                consumerRepository.findByConsumerId(consumerId))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    private Specification<Consumer> buildConsumerSpec(QueryConsumerParam param) {
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

    private Specification<ProductSubscription> buildCredentialSpec(String consumerId, QuerySubscriptionParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("consumerId"), consumerId));
            if (param.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), param.getStatus()));
            }
            if (StrUtil.isNotBlank(param.getProductName())) {
                // 关联Product表
                Join<ProductSubscription, Product> productJoin = root.join("product");
                predicates.add(cb.like(cb.lower(productJoin.get("name")), "%" + param.getProductName() + "%"));
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

//                deleteConsumer(consumer.getConsumerId());
                // TODO 清除凭证
            });
        } catch (Exception e) {
            log.error("Failed to clean consumers for developer {}", developerId, e);
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

    /**
     * 补充Credentials
     *
     * @param credential
     */
    private void complementCredentials(ConsumerCredential credential) {
        if (credential == null) {
            return;
        }

        // ApiKey
        if (credential.getApiKeyConfig() != null) {
            List<ApiKeyConfig.ApiKeyCredential> apiKeyCredentials = credential.getApiKeyConfig().getCredentials();
            if (apiKeyCredentials != null) {
                for (ApiKeyConfig.ApiKeyCredential cred : apiKeyCredentials) {
                    if (cred.getMode() == CredentialMode.SYSTEM && StrUtil.isBlank(cred.getApiKey())) {
                        cred.setApiKey(IdGenerator.genIdWithPrefix("apikey-"));
                    }
                }
            }
        }

        // HMAC
        if (credential.getHmacConfig() != null) {
            List<HmacConfig.HmacCredential> hmacCredentials = credential.getHmacConfig().getCredentials();
            if (hmacCredentials != null) {
                for (HmacConfig.HmacCredential cred : hmacCredentials) {
                    if (cred.getMode() == CredentialMode.SYSTEM &&
                            (StrUtil.isBlank(cred.getAk()) || StrUtil.isBlank(cred.getSk()))) {
                        cred.setAk(IdGenerator.genIdWithPrefix("ak-"));
                        cred.setSk(IdGenerator.genIdWithPrefix("sk-"));
                    }
                }
            }
        }
    }
}
