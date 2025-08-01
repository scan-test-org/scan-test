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

    Optional<Portal> findByPortalIdAndAdminId(String portalId, String adminId);

    Optional<Portal> findByPortalId(String portalId);

    Optional<Portal> findByNameAndAdminId(String name, String adminId);

    Optional<Portal> findByName(String name);

    Page<Portal> findByAdminId(String adminId, Pageable pageable);
}
