package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.NacosInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author zxd
 */
@Repository
public interface NacosInstanceRepository extends BaseRepository<NacosInstance, Long> {

    Optional<NacosInstance> findByNacosId(String nacosId);

    Optional<NacosInstance> findByNacosName(String nacosName);
} 