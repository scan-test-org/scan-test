package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ProductRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * API Reference Repository
 * @author zh
 */
@Repository
public interface ProductRefRepository extends JpaRepository<ProductRef, Long>, JpaSpecificationExecutor<ProductRef> {

    /**
     * 根据产品ID查找API引用列表
     */
    List<ProductRef> findByProductId(String productId);

    Optional<ProductRef> findFirstByProductId(String productId);

    /**
     * 根据API ID查找API引用
     */
    ProductRef findByApiId(String apiId);

    /**
     * 根据网关ID查找API引用列表
     */
    List<ProductRef> findByGatewayId(String gatewayId);

    /**
     * 根据Nacos ID查找API引用列表
     */
    List<ProductRef> findByNacosId(String nacosId);

    /**
     * 根据产品ID和网关ID查找API引用列表
     */
    List<ProductRef> findByProductIdAndGatewayId(String productId, String gatewayId);

    /**
     * 根据产品ID和Nacos ID查找API引用列表
     */
    List<ProductRef> findByProductIdAndNacosId(String productId, String nacosId);

    Optional<ProductRef> findByProductIdAndApiId(String productId, String apiId);
}
