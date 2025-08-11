package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ConsumerCredential;

import java.util.Optional;

/**
 * @author zh
 */
public interface ConsumerCredentialRepository extends BaseRepository<ConsumerCredential, Long> {

    Optional<ConsumerCredential> findByConsumerId(String consumerId);

    void deleteAllByConsumerId(String consumerId);
}
