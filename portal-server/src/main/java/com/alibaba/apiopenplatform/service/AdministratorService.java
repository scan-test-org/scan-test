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
     */
    Optional<Administrator> findByUsername(String username);

    /**
     * 根据管理员ID查询管理员
     *
     * @param adminId
     * @return
     */
    Optional<Administrator> findByAdminId(String adminId);

    /**
     * 管理员删除开发者
     *
     * @param developerId 开发者ID
     */
    void deleteDeveloper(String developerId);

    /**
     * 创建管理员
     */
    Administrator createAdministrator(AdminCreateParam param);

    /**
     * 校验用户名和密码，返回认证结果DTO
     */
    Optional<AuthResponseResult> loginWithPassword(String username, String password);

    /**
     * 检查指定portalId下是否需要初始化管理员
     */
    boolean needInit();

    /**
     * 初始化管理员，仅允许首次调用
     */
    Administrator initAdmin(String username, String password);

    /**
     * 管理员修改密码
     */
    boolean changePassword(String adminId, String oldPassword, String newPassword);
} 