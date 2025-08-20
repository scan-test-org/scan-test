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

    boolean existsByGatewayId(String gatewayId);
}
