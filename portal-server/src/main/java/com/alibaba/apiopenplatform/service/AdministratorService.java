package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.entity.Administrator;
import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateDto;
import com.alibaba.apiopenplatform.dto.result.AuthResponseDto;
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
     * 根据adminId查找管理员
     */
    Optional<Administrator> findByAdminId(String adminId);

    /**
     * 创建管理员
     */
    Administrator createAdministrator(AdminCreateDto createDto);

    /**
     * 多租户下校验用户名和密码，返回认证结果DTO
     */
    Optional<AuthResponseDto> loginWithPassword(String portalId, String username, String password);

    /**
     * 检查指定portalId下是否需要初始化管理员
     */
    boolean needInit(String portalId);

    /**
     * 初始化管理员，仅允许首次调用
     */
    Administrator initAdmin(String portalId, String username, String password);

    /**
     * 管理员修改密码
     */
    boolean changePassword(String portalId, String adminId, String oldPassword, String newPassword);
} 