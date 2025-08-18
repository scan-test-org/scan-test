package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ProductRef;
import com.alibaba.apiopenplatform.support.enums.SourceType;
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

    Optional<ProductRef> findByProductId(String productId);

    Optional<ProductRef> findFirstByProductId(String productId);

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

    /**
     * 根据数据源类型查找API引用列表
     */
    List<ProductRef> findBySourceType(SourceType sourceType);

    /**
     * 根据产品ID和数据源类型查找API引用
     */
    Optional<ProductRef> findByProductIdAndSourceType(String productId, SourceType sourceType);
}
