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
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.entity.ConsumerCredential;
import com.alibaba.apiopenplatform.repository.ConsumerRepository;
import com.alibaba.apiopenplatform.repository.ConsumerCredentialRepository;
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
        consumerRepository.delete(consumer);
    }

    @Override
    public ConsumerCredentialResult createCredential(String consumerId, CreateCredentialParam param) {
        // 校验consumer存在
        Consumer consumer = contextHolder.isDeveloper() ? findDevConsumer(consumerId) : findConsumer(consumerId);
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
}
