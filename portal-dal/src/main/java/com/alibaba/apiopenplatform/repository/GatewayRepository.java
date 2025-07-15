package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Gateway;

import java.util.Optional;

/**
 * @author zh
 */
public interface GatewayRepository extends BaseRepository<Gateway, Long> {

    Optional<Gateway> findByAdminIdAndGatewayId(String adminId, String gatewayId);
}
