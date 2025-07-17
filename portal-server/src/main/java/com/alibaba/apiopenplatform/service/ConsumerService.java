package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import org.springframework.data.domain.Pageable;

/**
 * @author zh
 */
public interface ConsumerService {

    /**
     * 注册Consumer
     *
     * @param param
     * @return
     */
    ConsumerResult registerConsumer(CreateConsumerParam param);


    /**
     * 审核Consumer
     *
     * @param consumerId
     * @return
     */
    void approveConsumer(String consumerId);

    /**
     * 创建Consumer
     *
     * @param param
     * @return
     */
    ConsumerResult createConsumer(CreateConsumerParam param);


    /**
     * 列出Developer的Consumer
     *
     * @param pageable
     * @return
     */
    PageResult<ConsumerResult> listConsumers(Pageable pageable);


    /**
     * 列出Portal的Consumer
     *
     * @param portalId
     * @param pageable
     * @return
     */
    PageResult<ConsumerResult> listConsumers(String portalId, Pageable pageable);


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
}
