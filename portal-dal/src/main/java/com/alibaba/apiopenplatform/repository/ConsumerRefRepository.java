package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ConsumerRef;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.gateway.GatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

/**
 * @author zh
 */
@Repository
public interface ConsumerRefRepository extends JpaRepository<ConsumerRef, Long>, JpaSpecificationExecutor<ConsumerRef> {

    List<ConsumerRef> findByConsumerId(String consumerId);

    @Query("SELECT c FROM ConsumerRef c WHERE c.consumerId = :consumerId AND c.gatewayType = :gatewayType AND c.gatewayConfig = :gatewayConfig")
    Optional<ConsumerRef> findConsumerRef(@Param("consumerId") String consumerId,
                                          @Param("gatewayType") GatewayType gatewayType,
                                          @Param("gatewayConfig") GatewayConfig gatewayConfig);

    Optional<ConsumerRef> findByGwConsumerId(String gwConsumerId);
}