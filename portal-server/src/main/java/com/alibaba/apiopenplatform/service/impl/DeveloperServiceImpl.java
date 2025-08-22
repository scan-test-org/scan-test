/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.event.DeveloperDeletingEvent;
import com.alibaba.apiopenplatform.core.event.PortalDeletingEvent;
import com.alibaba.apiopenplatform.core.utils.TokenUtil;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperCreateParam;
import com.alibaba.apiopenplatform.dto.params.developer.QueryDeveloperParam;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.dto.result.DeveloperResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.repository.DeveloperRepository;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.core.utils.PasswordHasher;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.repository.DeveloperExternalIdentityRepository;
import com.alibaba.apiopenplatform.entity.DeveloperExternalIdentity;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.support.enums.DeveloperStatus;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;

import javax.persistence.criteria.Predicate;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperServiceImpl implements DeveloperService {
    private final DeveloperRepository developerRepository;

    private final DeveloperExternalIdentityRepository developerExternalIdentityRepository;
    
    private final PortalService portalService;
    
    private final ContextHolder contextHolder;
    
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<Developer> findByDeveloperId(String developerId) {
        return developerRepository.findByDeveloperId(developerId);
    }

    @Override
    @Transactional
    public AuthResponseResult registerDeveloper(DeveloperCreateParam param) {
        Developer developer = createDeveloper(param);

        // 检查是否自动审批
        String portalId = contextHolder.getPortal();
        PortalResult portal = portalService.getPortal(portalId);
        boolean autoApprove = portal.getPortalSettingConfig() != null
                && BooleanUtil.isTrue(portal.getPortalSettingConfig().getAutoApproveDevelopers());

        if (autoApprove) {
            return generateAuthResult(developer);
        }
        return null;
    }

    @Override
    @Transactional
    public Developer createDeveloper(DeveloperCreateParam param) {
        String portalId = contextHolder.getPortal();
        developerRepository.findByPortalIdAndUsername(portalId, param.getUsername()).ifPresent(developer -> {
            throw new BusinessException(ErrorCode.DEVELOPER_USERNAME_EXISTS, param.getUsername());
        });

        Developer developer = param.convertTo();
        developer.setDeveloperId(generateDeveloperId());
        developer.setPortalId(portalId);
        developer.setPasswordHash(PasswordHasher.hash(param.getPassword()));
        
        PortalResult portal = portalService.getPortal(portalId);
        boolean autoApprove = portal.getPortalSettingConfig() != null
                && BooleanUtil.isTrue(portal.getPortalSettingConfig().getAutoApproveDevelopers());
        developer.setStatus(autoApprove ? DeveloperStatus.APPROVED : DeveloperStatus.PENDING);
        developer.setAuthType("LOCAL");
        
        return developerRepository.save(developer);
    }

    @Override
    public AuthResponseResult loginWithPassword(String username, String password) {
        String portalId = contextHolder.getPortal();
        Developer developer = findDeveloperByPortalAndUsername(portalId, username);
        
        if (!DeveloperStatus.APPROVED.equals(developer.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_PENDING);
        }
        if ("EXTERNAL".equals(developer.getAuthType()) || developer.getPasswordHash() == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_EXTERNAL_ONLY);
        }
        if (!PasswordHasher.verify(password, developer.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID);
        }
        
        String token = TokenUtil.generateDeveloperToken(developer.getDeveloperId());
        return AuthResponseResult.fromDeveloper(developer.getDeveloperId(), developer.getUsername(), token);
    }

    @Override
    @Transactional
    public Optional<AuthResponseResult> handleExternalLogin(String providerName, String providerSubject, String email, String displayName, String rawInfoJson) {
        Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(providerName, providerSubject);
        Developer developer;
        
        if (extOpt.isPresent()) {
            developer = extOpt.get().getDeveloper();
        } else {
            developer = createExternalDeveloper(providerName, providerSubject, email, displayName, rawInfoJson);
        }
        
        String token = generateToken(developer);
        return Optional.of(AuthResponseResult.fromDeveloper(developer.getDeveloperId(), developer.getUsername(), token));
    }

    @Override
    public AuthResponseResult generateAuthResult(Developer developer) {
        String token = generateToken(developer);
        return AuthResponseResult.fromDeveloper(developer.getDeveloperId(), developer.getUsername(), token);
    }

    @Override
    @Transactional
    public void bindExternalIdentity(String userId, String providerName, String providerSubject, String displayName, String rawInfoJson, String portalId) {
        validateOidcProvider(portalId, providerName);
        
        Developer developer = findDeveloper(userId);
        
        Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(providerName, providerSubject);
        if (extOpt.isPresent()) {
            String boundDevId = extOpt.get().getDeveloper().getDeveloperId();
            if (!boundDevId.equals(userId)) {
                throw new BusinessException(ErrorCode.EXTERNAL_IDENTITY_BOUND);
            }
            return; // 已绑定，无需重复绑定
        }
        
        DeveloperExternalIdentity ext = DeveloperExternalIdentity.builder()
                .provider(providerName)
                .subject(providerSubject)
                .displayName(displayName)
                .rawInfoJson(rawInfoJson)
                .developer(developer)
                .build();
        developerExternalIdentityRepository.save(ext);
    }

    @Override
    @Transactional
    public void unbindExternalIdentity(String userId, String providerName, String providerSubject) {
        String portalId = contextHolder.getPortal();
        validateOidcProvider(portalId, providerName);
        
        Developer developer = findDeveloper(userId);
        
        List<DeveloperExternalIdentity> identities = developerExternalIdentityRepository.findByDeveloper_DeveloperId(userId);
        boolean hasBuiltin = developer.getPasswordHash() != null;
        long otherCount = identities.stream()
                .filter(id -> !(id.getProvider().equals(providerName) && id.getSubject().equals(providerSubject)))
                .count();
        
        if (!hasBuiltin && otherCount == 0) {
            throw new BusinessException(ErrorCode.DEVELOPER_UNBIND_FAILED);
        }
        
        developerExternalIdentityRepository.deleteByProviderAndSubjectAndDeveloper_DeveloperId(providerName, providerSubject, userId);
    }

    @Override
    @Transactional
    public void deleteDeveloperAccount(String userId) {
        eventPublisher.publishEvent(new DeveloperDeletingEvent(userId));
        developerExternalIdentityRepository.deleteByDeveloper_DeveloperId(userId);
        developerRepository.findByDeveloperId(userId).ifPresent(developerRepository::delete);
    }

    @Override
    public boolean hasDeveloper(String portalId, String developerId) {
        return findDeveloper(developerId) != null;
    }

    @Override
    public DeveloperResult getDeveloper(String developerId) {
        Developer developer = findDeveloper(developerId);
        return new DeveloperResult().convertFrom(developer);
    }

    @Override
    public PageResult<DeveloperResult> listDevelopers(QueryDeveloperParam param, Pageable pageable) {
        if (contextHolder.isDeveloper()) {
            param.setPortalId(contextHolder.getPortal());
        }
        Page<Developer> developers = developerRepository.findAll(buildSpecification(param), pageable);
        return new PageResult<DeveloperResult>().convertFrom(developers, developer -> new DeveloperResult().convertFrom(developer));
    }

    @Override
    public void setDeveloperStatus(String developerId, DeveloperStatus status) {
        Developer developer = findDeveloper(developerId);
        developer.setStatus(status);
        developerRepository.save(developer);
    }

    @Override
    @Transactional
    public boolean changePassword(String developerId, String oldPassword, String newPassword) {
        Developer developer = findDeveloper(developerId);
        
        if (!PasswordHasher.verify(oldPassword, developer.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID);
        }
        
        developer.setPasswordHash(PasswordHasher.hash(newPassword));
        developerRepository.save(developer);
        return true;
    }

    @Override
    @Transactional
    public boolean updateProfile(String developerId, String username, String email, String avatarUrl) {
        Developer developer = findDeveloper(developerId);

        if (username != null && !username.equals(developer.getUsername())) {
            if (developerRepository.findByPortalIdAndUsername(developer.getPortalId(), username).isPresent()) {
                throw new BusinessException(ErrorCode.DEVELOPER_USERNAME_EXISTS, username);
            }
            developer.setUsername(username);
        }

        if (email != null) developer.setEmail(email);
        if (avatarUrl != null) developer.setAvatarUrl(avatarUrl);
        
        developerRepository.save(developer);
        return true;
    }

    @EventListener
    @Async("taskExecutor")
    public void handlePortalDeletion(PortalDeletingEvent event) {
        String portalId = event.getPortalId();
        List<Developer> developers = developerRepository.findByPortalId(portalId);
        developers.forEach(developer -> deleteDeveloperAccount(developer.getDeveloperId()));
    }

    private String generateToken(Developer developer) {
        // 统一使用 TokenUtil，确保 userType 按枚举大写（DEVELOPER），与鉴权角色映射一致
        return TokenUtil.generateDeveloperToken(developer.getDeveloperId());
    }

    private Developer createExternalDeveloper(String providerName, String providerSubject, String email, String displayName, String rawInfoJson) {
        String portalId = contextHolder.getPortal();
        String username = generateUniqueUsername(portalId, displayName, providerName, providerSubject);
        
        Developer developer = Developer.builder()
                .developerId(generateDeveloperId())
                .portalId(portalId)
                .username(username)
                .email(email)
                .status(DeveloperStatus.APPROVED)
                .authType("OIDC")
                .build();
        developer = developerRepository.save(developer);
        
        DeveloperExternalIdentity ext = DeveloperExternalIdentity.builder()
                .provider(providerName)
                .subject(providerSubject)
                .displayName(displayName)
                .rawInfoJson(rawInfoJson)
                .developer(developer)
                .build();
        developerExternalIdentityRepository.save(ext);
        return developer;
    }

    private String generateUniqueUsername(String portalId, String displayName, String providerName, String providerSubject) {
        String username = displayName != null ? displayName : providerName + "_" + providerSubject;
        String originalUsername = username;
        int suffix = 1;
        while (developerRepository.findByPortalIdAndUsername(portalId, username).isPresent()) {
            username = originalUsername + "_" + suffix;
            suffix++;
        }
        return username;
    }

    private void validateOidcProvider(String portalId, String providerName) {
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();
        
        boolean valid = portalSetting.getOidcConfigs() != null && 
                portalSetting.getOidcConfigs().stream()
                        .anyMatch(config -> providerName.equals(config.getProvider()) && config.isEnabled());
        if (!valid) {
            throw new BusinessException(ErrorCode.OIDC_CONFIG_DISABLED);
        }
    }

    private String generateDeveloperId() {
        return IdGenerator.genDeveloperId();
    }

    private Developer findDeveloper(String developerId) {
        return developerRepository.findByDeveloperId(developerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.DEVELOPER, developerId));
    }

    private Developer findDeveloperByPortalAndUsername(String portalId, String username) {
        return developerRepository.findByPortalIdAndUsername(portalId, username)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private Specification<Developer> buildSpecification(QueryDeveloperParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StrUtil.isNotBlank(param.getPortalId())) {
                predicates.add(cb.equal(root.get("portalId"), param.getPortalId()));
            }
            if (StrUtil.isNotBlank(param.getUsername())) {
                String likePattern = "%" + param.getUsername() + "%";
                predicates.add(cb.like(root.get("username"), likePattern));
            }
            if (param.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), param.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public void logout(HttpServletRequest request) {
        // 使用TokenUtil处理登出逻辑
        com.alibaba.apiopenplatform.core.utils.TokenUtil.revokeToken(request);
    }

    @Override
    public DeveloperResult getCurrentDeveloperInfo() {
        String currentUserId = contextHolder.getUser();
        Developer developer = findDeveloper(currentUserId);
        return new DeveloperResult().convertFrom(developer);
    }

    @Override
    public boolean changeCurrentDeveloperPassword(String oldPassword, String newPassword) {
        String currentUserId = contextHolder.getUser();
        return changePassword(currentUserId, oldPassword, newPassword);
    }

    @Override
    public boolean updateCurrentDeveloperProfile(String username, String email, String avatarUrl) {
        String currentUserId = contextHolder.getUser();
        return updateProfile(currentUserId, username, email, avatarUrl);
    }
}