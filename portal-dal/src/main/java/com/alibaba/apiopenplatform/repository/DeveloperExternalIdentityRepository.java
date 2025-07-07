package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.DeveloperExternalIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * 开发者外部身份数据访问接口，提供第三方认证信息的数据库操作
 *
 * @author zxd
 */
public interface DeveloperExternalIdentityRepository extends JpaRepository<DeveloperExternalIdentity, Long> {
    List<DeveloperExternalIdentity> findByDeveloper_DeveloperId(String developerId);
    Optional<DeveloperExternalIdentity> findByProviderAndSubject(String provider, String subject);
    void deleteByProviderAndSubjectAndDeveloper_DeveloperId(String provider, String subject, String developerId);
    void deleteByDeveloper_DeveloperId(String developerId);
} 