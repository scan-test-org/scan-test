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

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import com.alibaba.apiopenplatform.service.DeveloperService;

/**
 * 管理员服务实现类
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
        administratorRepository.findByUsername(param.getUsername()).ifPresent(admin -> {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, "username", param.getUsername());
        });
        
        Administrator admin = new Administrator();
        admin.setAdminId(generateAdminId());
        admin.setUsername(param.getUsername());
        admin.setPasswordHash(PasswordHasher.hash(param.getPassword()));
        return administratorRepository.save(admin);
    }

    @Override
    public Optional<AuthResponseResult> loginWithPassword(String username, String password) {
        Administrator admin = administratorRepository.findByUsername(username)
                .orElse(null);
        if (admin == null || !PasswordHasher.verify(password, admin.getPasswordHash())) {
            return Optional.empty();
        }
        
        String token = generateToken(admin);
        return Optional.of(AuthResponseResult.fromAdmin(admin.getAdminId(), admin.getUsername(), token));
    }

    @Override
    public void deleteDeveloper(String developerId) {
        developerService.deleteDeveloperAccount(developerId);
    }

    @Override
    public boolean needInit() {
        return administratorRepository.count() == 0;
    }

    @Override
    @Transactional
    public Administrator initAdmin(String username, String password) {
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
        Administrator admin = administratorRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "admin", adminId));
        
        if (!PasswordHasher.verify(oldPassword, admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID);
        }
        
        admin.setPasswordHash(PasswordHasher.hash(newPassword));
        administratorRepository.save(admin);
        return true;
    }

    private String generateToken(Administrator admin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", admin.getAdminId());
        claims.put("userType", "admin");
        return jwtService.generateToken("admin", admin.getAdminId(), claims);
    }

    private String generateAdminId() {
        return IdGenerator.genAdministratorId();
    }
} 