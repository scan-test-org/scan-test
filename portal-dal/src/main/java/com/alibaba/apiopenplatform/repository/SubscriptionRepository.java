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

    void deleteAllByConsumerId(String consumerId);

    void deleteAllByProductId(String productId);
}