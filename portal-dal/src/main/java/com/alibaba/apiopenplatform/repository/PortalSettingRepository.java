package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.PortalSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface PortalSettingRepository extends JpaRepository<PortalSetting, Long> {
    Optional<PortalSetting> findByPortalIdAndProvider(String portalId, String provider);

    java.util.List<PortalSetting> findByPortalId(String portalId);
} 