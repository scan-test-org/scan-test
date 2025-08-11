package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ConsumerRef;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

/**
 * Consumer Reference Repository
 * @author zh
 */
@Repository
public interface ConsumerRefRepository extends JpaRepository<ConsumerRef, Long>, JpaSpecificationExecutor<ConsumerRef> {

    List<ConsumerRef> findByConsumerId(String consumerId);

    @Query("SELECT c FROM ConsumerRef c WHERE c.consumerId = :consumerId AND c.gatewayType = :gatewayType AND c.gatewayIdentity = :gatewayIdentity")
    Optional<ConsumerRef> findConsumerRef(@Param("consumerId") String consumerId,
                                          @Param("gatewayType") GatewayType gatewayType,
                                          @Param("gatewayIdentity") String gatewayIdentity);

    /**
     * 根据Portal中的Consumer ID和网关类型查找引用
     */
    Optional<ConsumerRef> findByConsumerIdAndGatewayType(String consumerId, String gatewayType);

    /**
     * 根据网关Consumer ID查找引用
     */
    Optional<ConsumerRef> findByGwConsumerId(String gwConsumerId);

    /**
     * 根据网关类型查找引用列表
     */
    List<ConsumerRef> findByGatewayType(String gatewayType);
}