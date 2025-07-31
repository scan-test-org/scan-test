package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.dto.params.consumer.QueryConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.repository.ConsumerRepository;
import com.alibaba.apiopenplatform.service.ConsumerService;
import com.alibaba.apiopenplatform.service.GatewayService;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.support.enums.ConsumerStatus;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;

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
public class ConsumerServiceImpl implements ConsumerService {

    private final PortalService portalService;

    private final ConsumerRepository consumerRepository;

    private final GatewayService gatewayService;

    private final ContextHolder contextHolder;

    @Override
    public ConsumerResult registerConsumer(CreateConsumerParam param) {
        PortalResult portal = portalService.getPortal(contextHolder.getPortal());
        Consumer consumer = param.convertTo();
//        consumer.setDeveloperId(contextHolder.getUser());
        consumer.setDeveloperId("developer");

        // 审批策略
        PortalSettingConfig portalSettingConfig = portal.getPortalSettingConfig();
        ConsumerStatus status = BooleanUtil.isTrue(portalSettingConfig.getAutoApproveDevelopers()) ?
                ConsumerStatus.APPROVED : ConsumerStatus.PENDING;
        consumer.setStatus(status);
        consumerRepository.save(consumer);

        if (status == ConsumerStatus.APPROVED) {
            gatewayService.createConsumer(consumer);
        }
        return null;
    }

    @Override
    public void approveConsumer(String consumerId) {
        Consumer consumer = findConsumer(consumerId);

        gatewayService.createConsumer(consumer);

        consumer.setStatus(ConsumerStatus.APPROVED);
        consumerRepository.save(consumer);
    }

    @Override
    public ConsumerResult createConsumer(CreateConsumerParam param) {
        if (consumerRepository.findByDeveloperIdAndName("developer", param.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.CONSUMER, param.getName());
        }
        // OpenAPI调用

//        gatewayService.createConsumer()

        Consumer consumer = param.convertTo();
        consumer.setDeveloperId("developer");

        consumerRepository.save(consumer);
        ConsumerResult consumerResult = new ConsumerResult().convertFrom(consumer);

        // 填充Credential
        fullFillConsumer(consumerResult);
        return consumerResult;
    }

    @Override
    public PageResult<ConsumerResult> listConsumers(QueryConsumerParam param, Pageable pageable) {
        Page<Consumer> consumers = consumerRepository.findAll(buildSpecification(param), pageable);

        // List默认不返回Credential
        return new PageResult<ConsumerResult>().convertFrom(consumers, consumer -> new ConsumerResult().convertFrom(consumer));
    }

    @Override
    public ConsumerResult getConsumer(String consumerId) {
        Consumer consumer = contextHolder.isDeveloper() ? findDevConsumer(consumerId) : findConsumer(consumerId);

        ConsumerResult consumerResult = new ConsumerResult().convertFrom(consumer);
        fullFillConsumer(consumerResult);
        return consumerResult;
    }

    @Override
    public void deleteConsumer(String consumerId) {
        Consumer consumer = contextHolder.isDeveloper() ? findDevConsumer(consumerId) : findConsumer(consumerId);
        consumerRepository.delete(consumer);
    }

    private Consumer findConsumer(String consumerId) {
        return consumerRepository.findByConsumerId(consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    private Consumer findDevConsumer(String consumerId) {
        return consumerRepository.findByDeveloperIdAndConsumerId("developer", consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    private void fullFillConsumer(ConsumerResult consumerResult) {

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
}
