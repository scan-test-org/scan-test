package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.APIRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * API Reference Repository
 * @author zh
 */
@Repository
public interface APIRefRepository extends JpaRepository<APIRef, Long>, JpaSpecificationExecutor<APIRef> {

    /**
     * 根据产品ID查找API引用列表
     */
    List<APIRef> findByProductId(String productId);

    /**
     * 根据API ID查找API引用
     */
    APIRef findByApiId(String apiId);

    /**
     * 根据网关ID查找API引用列表
     */
    List<APIRef> findByGatewayId(String gatewayId);

    /**
     * 根据产品ID和网关ID查找API引用列表
     */
    List<APIRef> findByProductIdAndGatewayId(String productId, String gatewayId);

    /**
     * 根据产品ID和API ID查找API引用
     */
    APIRef findByProductIdAndApiId(String productId, String apiId);
}
