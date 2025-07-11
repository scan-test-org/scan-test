package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.dto.params.developer.DeveloperCreateDto;
import com.alibaba.apiopenplatform.dto.result.AuthResponseDto;
import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.repository.DeveloperRepository;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.core.utils.PasswordHasher;
import com.alibaba.apiopenplatform.auth.JwtService;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.repository.DeveloperExternalIdentityRepository;
import com.alibaba.apiopenplatform.entity.DeveloperExternalIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.repository.PortalSettingRepository;
import com.alibaba.apiopenplatform.entity.PortalSetting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 开发者服务实现类，负责开发者的注册、登录、查询等核心业务逻辑
 *
 * @author zxd
 */
@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {
    private final DeveloperRepository developerRepository;
    private final JwtService jwtService;
    private final DeveloperExternalIdentityRepository developerExternalIdentityRepository;
    private final PortalSettingRepository portalSettingRepository;
    private static final Logger log = LoggerFactory.getLogger(DeveloperServiceImpl.class);

    @Override
    public Optional<Developer> findByUsername(String username) {
        return developerRepository.findByUsername(username);
    }

    @Override
    public Optional<Developer> findByDeveloperId(String developerId) {
        return developerRepository.findByDeveloperId(developerId);
    }

    @Override
    @Transactional
    public Developer createDeveloper(DeveloperCreateDto createDto) {
        if (developerRepository.findByUsername(createDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        Developer developer = createDto.convertTo();
        developer.setDeveloperId(generateDeveloperId());
        developer.setPortalId(createDto.getPortalId());
        developer.setAvatarUrl(createDto.getAvatarUrl());
        developer.setPasswordHash(PasswordHasher.hash(createDto.getPassword()));
        developer.setStatus("ACTIVE");
        developer.setAuthType("LOCAL");
        return developerRepository.save(developer);
    }

    @Override
    public Optional<AuthResponseDto> loginWithPassword(String username, String password) {
        Optional<Developer> devOpt = developerRepository.findByUsername(username);
        if (!devOpt.isPresent()) {
            return Optional.empty();
        }
        Developer developer = devOpt.get();
        if (!"ACTIVE".equals(developer.getStatus())) {
            return Optional.empty();
        }
        if ("EXTERNAL".equals(developer.getAuthType()) || developer.getPasswordHash() == null) {
            // 外部身份用户不能本地密码登录
            return Optional.empty();
        }
        if (!PasswordHasher.verify(password, developer.getPasswordHash())) {
            return Optional.empty();
        }
        // 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", developer.getDeveloperId());
        claims.put("userType", "developer");
        String token = jwtService.generateToken(
                "developer", // userType
                developer.getDeveloperId(), // userId
                claims // extraClaims
        );
        AuthResponseDto dto = new AuthResponseDto();
        dto.setToken(token);
        dto.setUserId(developer.getDeveloperId());
        dto.setUsername(developer.getUsername());
        dto.setStatus(developer.getStatus());
        dto.setUserType("developer");
        return Optional.of(dto);
    }

    @Override
    @Transactional
    public Optional<AuthResponseDto> handleExternalLogin(String providerName, String providerSubject, String email, String displayName, String rawInfoJson) {
        log.info("[handleExternalLogin] providerName={}, providerSubject={}, email={}, displayName={}", providerName, providerSubject, email, displayName);
        // 1. 查找现有外部身份关联
        Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(providerName, providerSubject);
        Developer developer;
        if (extOpt.isPresent()) {
            // 已绑定，直接返回token
            DeveloperExternalIdentity ext = extOpt.get();
            developer = ext.getDeveloper();
            log.info("[handleExternalLogin] 已绑定外部身份，developerId={}, username={}", developer.getDeveloperId(), developer.getUsername());
            developerRepository.save(developer);
        } else {
            // 自动注册
            developer = new Developer();
            developer.setDeveloperId(generateDeveloperId());
            developer.setUsername(displayName != null ? displayName : providerName + "_" + providerSubject);
            developer.setPasswordHash(null);
            developer.setEmail(email);
            developer.setStatus("APPROVED");
            developer.setAuthType("OIDC");
            developer.setPortalId("default");
            developer = developerRepository.save(developer);
            log.info("[handleExternalLogin] 新注册开发者，developerId={}, username={}", developer.getDeveloperId(), developer.getUsername());
            // 绑定外部身份
            DeveloperExternalIdentity ext = new DeveloperExternalIdentity();
            ext.setProvider(providerName);
            ext.setSubject(providerSubject);
            ext.setDisplayName(displayName);
            ext.setRawInfoJson(rawInfoJson);
            ext.setDeveloper(developer);
            developerExternalIdentityRepository.save(ext);
            log.info("[handleExternalLogin] 新绑定外部身份，provider={}, subject={}, developerId={}", providerName, providerSubject, developer.getDeveloperId());
        }
        // 生成token
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", developer.getDeveloperId());
        claims.put("userType", "developer");
        String token = null;
        try {
            token = jwtService.generateToken(
                "developer",
                developer.getDeveloperId(),
                claims
            );
        } catch (Exception e) {
            log.error("[handleExternalLogin] 生成JWT异常: {}", e.getMessage(), e);
            return Optional.empty();
        }
        AuthResponseDto dto = new AuthResponseDto();
        dto.setToken(token);
        dto.setUserId(developer.getDeveloperId());
        dto.setUsername(developer.getUsername());
        dto.setStatus(developer.getStatus());
        dto.setUserType("developer");
        log.info("[handleExternalLogin] 返回JWT，developerId={}, token={}...", developer.getDeveloperId(), token != null ? token.substring(0, Math.min(16, token.length())) : null);
        return Optional.of(dto);
    }

    @Override
    @Transactional
    public void bindExternalIdentity(String userId, String providerName, String providerSubject, String displayName, String rawInfoJson, String portalId) {
        // 查库校验 providerName 是否为有效 provider，需传递 portalId
        PortalSetting setting = portalSettingRepository.findByPortalIdAndProvider(portalId, providerName)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "portal_setting", portalId + "," + providerName));
        if (setting.getOidcConfig() == null || !setting.getOidcConfig().isEnabled()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID, "OIDC配置未启用");
        }
        log.info("[bindExternalIdentity] userId={}, portalId={}, providerName={}, providerSubject={}", userId, portalId, providerName, providerSubject);
        Optional<Developer> devOpt = findByDeveloperId(userId);
        log.info("[bindExternalIdentity] findByDeveloperId({}) result: {}", userId, devOpt.isPresent() ? devOpt.get().getUsername() : "not found");
        if (!devOpt.isPresent()) {
            log.error("[bindExternalIdentity] 用户不存在，无法绑定，userId={}", userId);
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "developer", userId);
        }
        // 严格校验外部身份唯一性
        Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(providerName, providerSubject);
        if (extOpt.isPresent()) {
            String boundDevId = extOpt.get().getDeveloper().getDeveloperId();
            if (!boundDevId.equals(userId)) {
                log.error("[bindExternalIdentity] 该外部账号已被其他用户绑定，providerName={}, providerSubject={}, boundDevId={}", providerName, providerSubject, boundDevId);
                throw new BusinessException(ErrorCode.EXTERNAL_IDENTITY_BOUND);
            } else {
                log.info("[bindExternalIdentity] 该外部账号已被自己绑定，无需重复绑定");
                return;
            }
        }
        // 绑定到当前用户
        Developer developer = devOpt.get();
        DeveloperExternalIdentity ext = new DeveloperExternalIdentity();
        ext.setProvider(providerName);
        ext.setSubject(providerSubject);
        ext.setDisplayName(displayName);
        ext.setRawInfoJson(rawInfoJson);
        ext.setDeveloper(developer);
        developerExternalIdentityRepository.save(ext);
        log.info("[bindExternalIdentity] 绑定成功，userId={}, portalId={}, providerName={}, providerSubject={}", userId, portalId, providerName, providerSubject);
    }

    /**
     * 解绑外部身份（第三方登录）
     * @param userId 当前开发者ID
     * @param providerName 第三方类型
     * @param providerSubject 第三方唯一标识
     * @param portalId 门户唯一标识（建议前端传递）
     */
    @Transactional
    public void unbindExternalIdentity(String userId, String providerName, String providerSubject, String portalId) {
        // 新增：查库校验 providerName 是否为有效 provider
        portalSettingRepository.findByPortalIdAndProvider(portalId, providerName)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "portal_setting", portalId + "," + providerName));
        // 查找该用户所有外部身份
        java.util.List<DeveloperExternalIdentity> identities = developerExternalIdentityRepository.findByDeveloper_DeveloperId(userId);
        // 查找开发者
        Developer developer = developerRepository.findByDeveloperId(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        boolean hasBuiltin = developer.getPasswordHash() != null;
        long otherCount = identities.stream()
            .filter(id -> !(id.getProvider().equals(providerName) && id.getSubject().equals(providerSubject)))
            .count();
        if (!hasBuiltin && otherCount == 0) {
            throw new RuntimeException("解绑失败，账号至少保留一种登录方式");
        }
        // 删除外部身份
        developerExternalIdentityRepository.deleteByProviderAndSubjectAndDeveloper_DeveloperId(providerName, providerSubject, userId);
        log.info("[unbindExternalIdentity] 解绑成功，userId={}, portalId={}, providerName={}, providerSubject={}", userId, portalId, providerName, providerSubject);
    }

    /**
     * 注销开发者账号（删除账号及所有外部身份）
     * @param userId 当前开发者ID
     */
    @Transactional
    public void deleteDeveloperAccount(String userId) {
        // 删除所有外部身份
        developerExternalIdentityRepository.deleteByDeveloper_DeveloperId(userId);
        // 删除开发者主表
        developerRepository.findByDeveloperId(userId).ifPresent(developerRepository::delete);
        // TODO: 删除其他相关数据（如token、个人信息、日志等，视业务而定）
    }

    private String generateDeveloperId() {
        return IdGenerator.genDeveloperId();
    }
} 