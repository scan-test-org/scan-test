package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.core.utils.TokenUtil;
import com.alibaba.apiopenplatform.dto.result.AdminResult;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.entity.Administrator;
import com.alibaba.apiopenplatform.repository.AdministratorRepository;
import com.alibaba.apiopenplatform.service.AdministratorService;
import com.alibaba.apiopenplatform.core.utils.PasswordHasher;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;

/**
 * @author zxd
 */
@Service
@RequiredArgsConstructor
public class AdministratorServiceImpl implements AdministratorService {

    private final AdministratorRepository administratorRepository;

    private final ContextHolder contextHolder;

    @Override
    public AuthResponseResult login(String username, String password) {
        Administrator admin = administratorRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID));

        if (!PasswordHasher.verify(password, admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID);
        }

        String token = TokenUtil.generateAdminToken(admin.getAdminId());
        return AuthResponseResult.fromAdmin(admin.getAdminId(), admin.getUsername(), token);
    }

    @Override
    public boolean needInit() {
        return administratorRepository.count() == 0;
    }

    @Override
    @Transactional
    public AdminResult initAdmin(String username, String password) {
        if (!needInit()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.ADMINISTRATOR, null);
        }

        Administrator admin = new Administrator();
        admin.setAdminId(generateAdminId());
        admin.setUsername(username);
        admin.setPasswordHash(PasswordHasher.hash(password));
        administratorRepository.save(admin);
        return new AdminResult().convertFrom(admin);
    }

    @Override
    public AdminResult getAdministrator() {
        Administrator administrator = findAdministrator(contextHolder.getUser());
        return new AdminResult().convertFrom(administrator);
    }

    @Override
    @Transactional
    public void resetPassword(String oldPassword, String newPassword) {
        Administrator admin = findAdministrator(contextHolder.getUser());

        if (!PasswordHasher.verify(oldPassword, admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID);
        }

        admin.setPasswordHash(PasswordHasher.hash(newPassword));
        administratorRepository.save(admin);
    }

    private String generateAdminId() {
        return IdGenerator.genAdministratorId();
    }

    private Administrator findAdministrator(String adminId) {
        return administratorRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.ADMINISTRATOR, adminId));
    }
} 