package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.consumer.QueryConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ConsumerCredentialResult;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateCredentialParam;
import com.alibaba.apiopenplatform.dto.params.consumer.UpdateCredentialParam;
import com.alibaba.apiopenplatform.dto.result.SubscriptionResult;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateSubscriptionParam;
import com.alibaba.apiopenplatform.dto.params.consumer.QuerySubscriptionParam;
import org.springframework.data.domain.Pageable;

/**
 * @author zh
 */
public interface ConsumerService {

    /**
     * 创建Consumer
     *
     * @param param
     * @return
     */
    ConsumerResult createConsumer(CreateConsumerParam param);

    /**
     * 获取Consumer列表
     *
     * @param param
     * @param pageable
     * @return
     */
    PageResult<ConsumerResult> listConsumers(QueryConsumerParam param, Pageable pageable);


    /**
     * 查询Consumer
     *
     * @param consumerId
     * @return
     */
    ConsumerResult getConsumer(String consumerId);

    /**
     * 删除Consumer
     *
     * @param consumerId
     */
    void deleteConsumer(String consumerId);

    /**
     * 创建Consumer凭证
     *
     * @param consumerId
     * @param param
     */
    void createCredential(String consumerId, CreateCredentialParam param);

    /**
     * 获取Consumer凭证
     *
     * @param consumerId
     * @return
     */
    ConsumerCredentialResult getCredential(String consumerId);

    /**
     * 更新Consumer凭证
     *
     * @param consumerId
     * @param param
     */
    void updateCredential(String consumerId, UpdateCredentialParam param);

    /**
     * 删除Consumer凭证
     *
     * @param consumerId Consumer ID
     */
    void deleteCredential(String consumerId);

    /**
     * 订阅API产品
     *
     * @param consumerId
     * @param param
     * @return
     */
    SubscriptionResult subscribeProduct(String consumerId, CreateSubscriptionParam param);

    /**
     * 取消订阅
     *
     * @param consumerId
     * @param productId
     */
    void unsubscribeProduct(String consumerId, String productId);

    /**
     * 获取Consumer的订阅列表
     *
     * @param consumerId
     * @param param
     * @param pageable
     * @return
     */
    PageResult<SubscriptionResult> listSubscriptions(String consumerId, QuerySubscriptionParam param, Pageable pageable);

    /**
     * 取消订阅API产品
     *
     * @param consumerId
     * @param productId
     */
    void deleteSubscription(String consumerId, String productId);

    /**
     * 审批订阅API产品
     *
     * @param consumerId
     * @param productId
     */
    SubscriptionResult approveSubscription(String consumerId, String productId);
}
