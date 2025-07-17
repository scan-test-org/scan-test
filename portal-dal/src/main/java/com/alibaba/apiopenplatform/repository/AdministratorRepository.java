package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 管理员数据访问接口，提供管理员相关的数据库操作
 *
 * @author zxd
 */
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByAdminId(String adminId);
    Optional<Administrator> findByUsername(String username);
} 