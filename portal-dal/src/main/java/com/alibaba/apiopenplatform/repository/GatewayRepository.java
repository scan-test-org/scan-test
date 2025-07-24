package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Gateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author zh
 */
public interface GatewayRepository extends BaseRepository<Gateway, Long> {

    Optional<Gateway> findByGatewayId(String gatewayId);

    Page<Gateway> findByAdminId(String adminId, Pageable pageable);
}
