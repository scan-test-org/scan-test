package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Portal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author zh
 */
@Repository
public interface PortalRepository extends JpaRepository<Portal, Long>, JpaSpecificationExecutor<Portal> {

    /**
     * 根据PortalId查询
     *
     * @param portalId
     * @return
     */
    Optional<Portal> findByPortalId(String portalId);

    /**
     * 根据名称和管理员ID查询
     *
     * @param name
     * @return
     */
    Optional<Portal> findByNameAndAdminId(String name, String adminId);
}
