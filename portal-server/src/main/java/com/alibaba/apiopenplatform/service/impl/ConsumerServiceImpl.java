package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.dto.result.PortalSettingConfig;
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.repository.ConsumerRepository;
import com.alibaba.apiopenplatform.service.ConsumerService;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.service.GatewayService;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.service.gateway.client.APIGClient;
import com.alibaba.apiopenplatform.service.gateway.client.HigressClient;
import com.alibaba.apiopenplatform.support.enums.ConsumerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

    private final DeveloperService developerService;

    @Override
    public ConsumerResult registerConsumer(CreateConsumerParam param) {
        PortalResult portal = portalService.getPortal(param.getPortalId());
        Consumer consumer = param.convertTo();
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
    public PageResult<ConsumerResult> listConsumers(Pageable pageable) {
        Page<Consumer> consumers = consumerRepository.findByDeveloperId("developer", pageable);

        // List默认不返回Credential
        Page<ConsumerResult> pages = consumers.map(consumer -> new ConsumerResult().convertFrom(consumer));
        return new PageResult<ConsumerResult>().convertFrom(pages);
    }

    @Override
    public PageResult<ConsumerResult> listConsumers(String portalId, Pageable pageable) {
        portalService.hasPortal(portalId);
        Page<Consumer> consumers = consumerRepository.findByPortalId(portalId, pageable);

        Page<ConsumerResult> pages = consumers.map(consumer -> new ConsumerResult().convertFrom(consumer));
        return new PageResult<ConsumerResult>().convertFrom(pages);
    }

    @Override
    public ConsumerResult getConsumer(String consumerId) {
        Consumer consumer = findConsumer(consumerId);

        ConsumerResult consumerResult = new ConsumerResult().convertFrom(consumer);
        fullFillConsumer(consumerResult);
        return consumerResult;
    }

    @Override
    public void deleteConsumer(String consumerId) {
        Consumer consumer = findConsumer(consumerId);
        consumerRepository.delete(consumer);
    }

    private Consumer findConsumer(String consumerId) {
        return consumerRepository.findByDeveloperIdAndConsumerId("developer", consumerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.CONSUMER, consumerId));
    }

    private void fullFillConsumer(ConsumerResult consumerResult) {

    }
}
