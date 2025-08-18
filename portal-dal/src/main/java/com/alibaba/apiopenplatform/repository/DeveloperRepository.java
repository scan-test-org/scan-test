package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Developer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/**
 * 开发者实体 JPA 仓库接口
 * 提供开发者相关的数据库操作方法
 *
 * @author zxd
 */
public interface DeveloperRepository extends BaseRepository<Developer, Long> {
    Optional<Developer> findByDeveloperId(String developerId);

    Optional<Developer> findByUsername(String username);

    List<Developer> findByPortalId(String portalId);

    Optional<Developer> findByPortalIdAndUsername(String portalId, String username);

    Optional<Developer> findByPortalIdAndEmail(String portalId, String email);

    Optional<Developer> findByDeveloperIdAndPortalId(String developerId, String portalId);

    Page<Developer> findByPortalId(String portalId, Pageable pageable);
} 