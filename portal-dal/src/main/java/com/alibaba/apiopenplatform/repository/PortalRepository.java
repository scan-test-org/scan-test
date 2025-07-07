package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Portal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author zh
 */
@Repository
public interface PortalRepository extends JpaRepository<Portal, Long>, JpaSpecificationExecutor<Portal> {

    @EntityGraph("portal.properties")
    Optional<Portal> findByPortalIdAndAdminId(String portalId, String adminId);

    @EntityGraph("portal.properties")
    Optional<Portal> findByNameAndAdminId(String name, String adminId);

    @EntityGraph("portal.properties")
    Page<Portal> findByAdminId(String adminId, Pageable pageable);
}
