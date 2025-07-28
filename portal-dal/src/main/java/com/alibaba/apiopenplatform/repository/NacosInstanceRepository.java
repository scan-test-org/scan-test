package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.NacosInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Nacos实例Repository
 * @author zxd
 */
@Repository
public interface NacosInstanceRepository extends BaseRepository<NacosInstance, Long> {
    
    /**
     * 根据nacosId查找实例
     */
    Optional<NacosInstance> findByNacosId(String nacosId);
    
    /**
     * 根据管理员ID查找实例列表
     */
    List<NacosInstance> findByAdminId(String adminId);
    
    /**
     * 根据管理员ID分页查询实例
     */
    Page<NacosInstance> findByAdminId(String adminId, Pageable pageable);
    
    /**
     * 根据nacosName和adminId查找实例
     */
    Optional<NacosInstance> findByNacosNameAndAdminId(String nacosName, String adminId);
} 