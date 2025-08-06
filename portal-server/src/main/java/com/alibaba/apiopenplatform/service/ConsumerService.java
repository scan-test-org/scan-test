package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.consumer.QueryConsumerParam;
import com.alibaba.apiopenplatform.dto.params.consumer.CreateConsumerParam;
import com.alibaba.apiopenplatform.dto.result.ConsumerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
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
}
