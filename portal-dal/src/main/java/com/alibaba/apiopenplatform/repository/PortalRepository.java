package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Portal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

/**
 * @author zh
 */
public interface PortalRepository extends BaseRepository<Portal, Long> {

    @EntityGraph("portal.properties")
    Optional<Portal> findByPortalIdAndAdminId(String portalId, String adminId);

    @EntityGraph("portal.properties")
    Optional<Portal> findByPortalId(String portalId);

    @EntityGraph("portal.properties")
    Optional<Portal> findByNameAndAdminId(String name, String adminId);

    @EntityGraph("portal.properties")
    Page<Portal> findByAdminId(String adminId, Pageable pageable);
}
