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

    Page<ProductPublication> findByPortalId(String portalId, Pageable pageable);

    Optional<ProductPublication> findByPortalIdAndProductId(String portalId, String productId);

    void deleteByProductId(String productId);
}
