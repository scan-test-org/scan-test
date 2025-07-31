package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateParam;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.entity.Administrator;
import com.alibaba.apiopenplatform.repository.AdministratorRepository;
import com.alibaba.apiopenplatform.service.AdministratorService;
import com.alibaba.apiopenplatform.core.utils.PasswordHasher;
import com.alibaba.apiopenplatform.auth.JwtService;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import com.alibaba.apiopenplatform.service.DeveloperService;

/**
 * 管理员服务实现类，负责管理员的注册、登录、查询等核心业务逻辑
 *
 * @author zxd
 */
@Service
@RequiredArgsConstructor
public class AdministratorServiceImpl implements AdministratorService {
    private final AdministratorRepository administratorRepository;
    private final JwtService jwtService;
    private final DeveloperService developerService;

    @Override
    public Optional<Administrator> findByUsername(String username) {
        return administratorRepository.findByUsername(username);
    }

    @Override
    public Optional<Administrator> findByAdminId(String adminId) {
        return administratorRepository.findByAdminId(adminId);
    }

    @Override
    @Transactional
    public Administrator createAdministrator(AdminCreateParam param) {
        // 检查用户名唯一性
        if (administratorRepository.findByUsername(param.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, "username", param.getUsername());
        }
        Administrator admin = new Administrator();
        admin.setAdminId(generateAdminId());
        admin.setUsername(param.getUsername());
        admin.setPasswordHash(PasswordHasher.hash(param.getPassword()));
        return administratorRepository.save(admin);
    }

    @Override
    public Optional<AuthResponseResult> loginWithPassword(String username, String password) {
        Optional<Administrator> adminOpt = administratorRepository.findByUsername(username);
        if (!adminOpt.isPresent()) {
            return Optional.empty();
        }
        Administrator admin = adminOpt.get();
        if (!PasswordHasher.verify(password, admin.getPasswordHash())) {
            return Optional.empty();
        }
        // 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", admin.getAdminId());
        claims.put("userType", "admin");
        String token = jwtService.generateToken(
                "admin", // userType
                admin.getAdminId(), // userId
                claims // extraClaims
        );
        AuthResponseResult dto = new AuthResponseResult();
        dto.setToken(token);
        dto.setUserId(admin.getAdminId());
        dto.setUsername(admin.getUsername());
        dto.setUserType("admin");
        return Optional.of(dto);
    }

    @Override
    public void deleteDeveloper(String developerId) {
        // 调用开发者服务删除开发者账号
        developerService.deleteDeveloperAccount(developerId);
    }

    @Override
    public boolean needInit() {
        // 只要管理员表无任何记录就允许初始化
        return administratorRepository.count() == 0;
    }

    @Override
    @Transactional
    public Administrator initAdmin(String username, String password) {
        // 只允许首次初始化（全表无记录）
        if (!needInit()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, "admin", null);
        }
        Administrator admin = new Administrator();
        admin.setAdminId(generateAdminId());
        admin.setUsername(username);
        admin.setPasswordHash(PasswordHasher.hash(password));
        return administratorRepository.save(admin);
    }

    @Override
    @Transactional
    public boolean changePassword(String adminId, String oldPassword, String newPassword) {
        Optional<Administrator> adminOpt = administratorRepository.findByAdminId(adminId);
        if (!adminOpt.isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "admin", adminId);
        }
        Administrator admin = adminOpt.get();
        if (!PasswordHasher.verify(oldPassword, admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID);
        }
        admin.setPasswordHash(PasswordHasher.hash(newPassword));
        return true;
    }

    private String generateAdminId() {
        return IdGenerator.genAdministratorId();
    }
} 