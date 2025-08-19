package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.entity.Administrator;
import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateParam;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import java.util.Optional;

/**
 * 管理员服务接口，定义管理员相关的核心操作方法
 *
 * @author zxd
 */
public interface AdministratorService {

    /**
     * 根据用户名查找管理员
     *
     * @param username 管理员用户名
     * @return 管理员信息，如果不存在则返回空
     */
    Optional<Administrator> findByUsername(String username);

    /**
     * 根据管理员ID查询管理员
     *
     * @param adminId 管理员唯一标识
     * @return 管理员信息，如果不存在则返回空
     */
    Optional<Administrator> findByAdminId(String adminId);

    /**
     * 创建管理员
     *
     * @param param 管理员创建参数
     * @return 创建成功的管理员信息
     */
    Administrator createAdministrator(AdminCreateParam param);

    /**
     * 校验用户名和密码，返回认证结果DTO
     *
     * @param username 管理员用户名
     * @param password 管理员密码
     * @return 认证结果，如果认证失败则返回空
     */
    Optional<AuthResponseResult> loginWithPassword(String username, String password);

    /**
     * 管理员修改密码
     *
     * @param adminId 管理员唯一标识
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改是否成功
     */
    boolean changePassword(String adminId, String oldPassword, String newPassword);

    /**
     * 检查指定portalId下是否需要初始化管理员
     *
     * @return 是否需要初始化管理员
     */
    boolean needInit();

    /**
     * 初始化管理员，仅允许首次调用
     *
     * @param username 管理员用户名
     * @param password 管理员密码
     * @return 初始化成功的管理员信息
     */
    Administrator initAdmin(String username, String password);

    /**
     * 管理员删除开发者
     *
     * @param developerId 开发者唯一标识
     */
    void deleteDeveloper(String developerId);
} 