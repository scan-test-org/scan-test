package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author zh
 */
public interface ConsumerRepository extends BaseRepository<Consumer, Long> {

    Optional<Consumer> findByConsumerId(String consumerId);

    Optional<Consumer> findByDeveloperIdAndConsumerId(String developerId, String consumerId);

    Optional<Consumer> findByDeveloperIdAndName(String developerId, String name);

    Page<Consumer> findByDeveloperId(String developerId, Pageable pageable);

    Page<Consumer> findByPortalId(String portalId, Pageable pageable);
}
