package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateDto;
import com.alibaba.apiopenplatform.dto.result.AuthResponseDto;
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
    public Administrator createAdministrator(AdminCreateDto createDto) {
        // 检查用户名唯一性
        if (administratorRepository.findByUsername(createDto.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, "username", createDto.getUsername());
        }
        Administrator admin = new Administrator();
        admin.setAdminId(generateAdminId());
        admin.setUsername(createDto.getUsername());
        admin.setPasswordHash(PasswordHasher.hash(createDto.getPassword()));
        return administratorRepository.save(admin);
    }

    @Override
    public Optional<AuthResponseDto> loginWithPassword(String username, String password) {
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
        AuthResponseDto dto = new AuthResponseDto();
        dto.setToken(token);
        dto.setUserId(admin.getAdminId());
        dto.setUsername(admin.getUsername());
        dto.setUserType("admin");
        return Optional.of(dto);
    }

    @Override
    public boolean needInit(String portalId) {
        // 只要管理员表无任何记录就允许初始化
        return administratorRepository.count() == 0;
    }

    @Override
    @Transactional
    public Administrator initAdmin(String portalId, String username, String password) {
        // 只允许首次初始化（全表无记录）
        if (!needInit(null)) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, "admin", portalId);
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