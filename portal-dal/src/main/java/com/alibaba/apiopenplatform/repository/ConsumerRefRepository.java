package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ConsumerRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Consumer Reference Repository
 * @author zh
 */
@Repository
public interface ConsumerRefRepository extends JpaRepository<ConsumerRef, Long>, JpaSpecificationExecutor<ConsumerRef> {

    /**
     * 根据Portal中的Consumer ID查找引用列表
     */
    List<ConsumerRef> findByConsumerId(String consumerId);

    /**
     * 根据Portal中的Consumer ID和网关类型查找引用
     */
    Optional<ConsumerRef> findByConsumerIdAndGatewayType(String consumerId, String gatewayType);

    /**
     * 根据网关Consumer ID查找引用
     */
    Optional<ConsumerRef> findByGwConsumerId(String gwConsumerId);

    /**
     * 根据Portal中的Consumer ID、UID、Region、GatewayType查找引用
     */
    Optional<ConsumerRef> findByConsumerIdAndRegionAndGatewayType(String consumerId, String region, String gatewayType);

    /**
     * 根据网关类型查找引用列表
     */
    List<ConsumerRef> findByGatewayType(String gatewayType);

    /**
     * 根据UID和网关类型查找引用列表
     */
    List<ConsumerRef> findByUidAndGatewayType(String uid, String gatewayType);
} 