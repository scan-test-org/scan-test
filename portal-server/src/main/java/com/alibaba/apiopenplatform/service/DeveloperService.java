package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.developer.DeveloperCreateParam;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.dto.result.DeveloperResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.Developer;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 开发者服务接口，定义注册、登录、查找等核心方法
 *
 * @author zxd
 */
public interface DeveloperService {
    Optional<Developer> findByUsername(String username);

    Optional<Developer> findByDeveloperId(String developerId);

    Developer createDeveloper(DeveloperCreateParam param);

    AuthResponseResult loginWithPassword(String username, String password);

    /**
     * 外部身份登录/绑定入口
     *
     * @param providerName    外部身份提供商
     * @param providerSubject 外部身份唯一标识
     * @param email           外部邮箱
     * @param displayName     第三方显示名
     * @param rawInfoJson     第三方原始信息JSON
     * @return 登录结果
     */
    Optional<AuthResponseResult> handleExternalLogin(String providerName, String providerSubject, String email, String displayName, String rawInfoJson);

    /**
     * 绑定外部身份（不切换登录态，仅写数据库）
     *
     * @param userId          本地用户ID
     * @param providerName    外部身份提供商
     * @param providerSubject 外部身份唯一标识
     * @param displayName     第三方显示名
     * @param rawInfoJson     第三方原始信息JSON
     * @param portalId        门户唯一标识
     * @param displayName     第三方显示名
     * @param rawInfoJson     第三方原始信息JSON
     */
    void bindExternalIdentity(String userId, String providerName, String providerSubject, String displayName, String rawInfoJson, String portalId);

    /**
     * 为开发者生成认证结果（用于注册后自动登录）
     *
     * @param developer 开发者实体
     * @return 认证结果
     */
    AuthResponseResult generateAuthResult(Developer developer);

    /**
     * 解绑外部身份（第三方登录）
     *
     * @param userId          当前开发者ID
     * @param providerName    第三方类型
     * @param providerSubject 第三方唯一标识
     * @param portalId        门户唯一标识
     */
    void unbindExternalIdentity(String userId, String providerName, String providerSubject, String portalId);

    /**
     * 注销开发者账号（删除账号及所有外部身份）
     *
     * @param userId 当前开发者ID
     */
    void deleteDeveloperAccount(String userId);

    boolean hasDeveloper(String portalId, String developerId);

    /**
     * 查询开发者详情
     *
     * @param developerId
     * @return
     */
    DeveloperResult getDeveloper(String developerId);

    /**
     * 查询门户下的开发者列表
     *
     * @param portalId
     * @param pageable
     * @return
     */
    PageResult<DeveloperResult> listDevelopers(String portalId, Pageable pageable);


    /**
     * 设置开发者状态
     *
     * @param portalId
     * @param developerId
     * @param status
     * @return
     */
    void setDeveloperStatus(String portalId, String developerId, String status);

    /**
     * 开发者修改密码
     *
     * @param developerId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    boolean changePassword(String developerId, String oldPassword, String newPassword);

    /**
     * 开发者更新个人信息
     *
     * @param developerId
     * @param username
     * @param email
     * @param avatarUrl
     * @return
     */
    boolean updateProfile(String developerId, String username, String email, String avatarUrl);
}