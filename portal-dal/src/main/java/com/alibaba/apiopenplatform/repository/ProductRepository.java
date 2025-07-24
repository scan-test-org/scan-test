package com.alibaba.apiopenplatform.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Collection;
import java.util.Optional;

import com.alibaba.apiopenplatform.entity.Product;

@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {

    Optional<Product> findByProductId(String productId);

    Optional<Product> findByProductIdAndAdminId(String productId, String adminId);

    Optional<Product> findByNameAndAdminId(String name, String adminId);

    Page<Product> findByProductIdIn(Collection<String> productIds, Pageable pageable);

    Page<Product> findByAdminId(String adminId, Pageable pageable);

    Page<Product> findByType(String type, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);
}
