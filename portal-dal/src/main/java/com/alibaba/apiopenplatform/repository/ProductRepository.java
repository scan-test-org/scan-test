package com.alibaba.apiopenplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;


import java.util.Optional;

import com.alibaba.apiopenplatform.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    @EntityGraph("product.properties")
    Product findByProductId(String productId);

    @EntityGraph("product.properties")
    Optional<Product> findByNameAndOwnerId(String name, String ownerId);

    @EntityGraph("product.properties")
    Page<Product> findByOwnerId(String ownerId, Pageable pageable);

    @EntityGraph("product.properties")
    Optional<Product> findByProductIdAndOwnerId(String productId, String ownerId);

    @EntityGraph("product.properties")
    Page<Product> findByStatus(String status, Pageable pageable);

    @EntityGraph("product.properties")
    Page<Product> findByType(String type, Pageable pageable);

    @EntityGraph("product.properties")
    Page<Product> findByCategory(String category, Pageable pageable);

    @EntityGraph("product.properties")
    Page<Product> findByStatusAndType(String status, String type, Pageable pageable);
}
