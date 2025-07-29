package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ProductPublication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

/**
 * @author zh
 */
public interface ProductPublicationRepository extends BaseRepository<ProductPublication, Long> {

    @EntityGraph("product.properties")
    Page<ProductPublication> findByPortalId(String portalId, Pageable pageable);

    @EntityGraph("product.properties")
    Optional<ProductPublication> findByPortalIdAndProductId(String portalId, String productId);
}
