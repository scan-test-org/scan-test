package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.PortalDomain;

import java.util.Optional;

/**
 * @author zh
 */
public interface PortalDomainRepository extends BaseRepository<PortalDomain, Long> {

    Optional<PortalDomain> findByDomain(String domain);

    Optional<PortalDomain> findByPortalIdAndDomain(String portalId, String domain);
}
