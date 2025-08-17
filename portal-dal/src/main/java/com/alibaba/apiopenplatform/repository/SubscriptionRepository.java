package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ProductSubscription;

import java.util.List;
import java.util.Optional;

/**
 * @author zh
 */
public interface SubscriptionRepository extends BaseRepository<ProductSubscription, Long> {

    Optional<ProductSubscription> findByConsumerIdAndProductId(String consumerId, String productId);

    List<ProductSubscription> findALlByConsumerId(String consumerId);

    List<ProductSubscription> findAllByProductId(String productId);

    void deleteAllByConsumerId(String consumerId);

    void deleteAllByProductId(String productId);

    void deleteByConsumerIdAndProductId(String consumerId, String productId);
}