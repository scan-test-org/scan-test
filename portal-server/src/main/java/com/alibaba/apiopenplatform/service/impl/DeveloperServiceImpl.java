package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.event.DeveloperDeletingEvent;
import com.alibaba.apiopenplatform.core.event.PortalDeletingEvent;
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
import com.alibaba.apiopenplatform.auth.JwtService;
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
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.core.security.ContextHolder;

import javax.persistence.criteria.Predicate;
import java.util.*;

/**
 * 开发者服务实现类，负责开发者的注册、登录、查询等核心业务逻辑
 *
 * @author zxd
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperServiceImpl implements DeveloperService {
    private final DeveloperRepository developerRepository;
    private final JwtService jwtService;
    private final DeveloperExternalIdentityRepository developerExternalIdentityRepository;
    private final PortalRepository portalRepository;
    private final PortalService portalService;
    private final ContextHolder contextHolder;

    private final ApplicationEventPublisher eventPublisher;

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
    public Developer createDeveloper(DeveloperCreateParam param) {
        developerRepository.findByUsername(param.getUsername()).ifPresent(developer -> {
            throw new BusinessException(ErrorCode.DEVELOPER_USERNAME_EXISTS, param.getUsername());
        });

        Developer developer = param.convertTo();
        developer.setDeveloperId(generateDeveloperId());

        // 从当前请求上下文中获取portalId
        String portalId = contextHolder.getPortal();
        developer.setPortalId(portalId);

        developer.setPasswordHash(PasswordHasher.hash(param.getPassword()));
        // 根据门户配置决定是否自动审批
        PortalResult portal = portalService.getPortal(portalId);

        boolean autoApprove = portal.getPortalSettingConfig() != null
                && BooleanUtil.isTrue(portal.getPortalSettingConfig().getAutoApproveDevelopers());
        developer.setStatus(autoApprove ? DeveloperStatus.APPROVED : DeveloperStatus.PENDING);
        developer.setAuthType("LOCAL");
        return developerRepository.save(developer);
    }

    @Override
    public AuthResponseResult loginWithPassword(String username, String password) {
        Optional<Developer> devOpt = developerRepository.findByUsername(username);
        if (!devOpt.isPresent()) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        Developer developer = devOpt.get();
        if (!DeveloperStatus.APPROVED.equals(developer.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_PENDING);
        }
        if ("EXTERNAL".equals(developer.getAuthType()) || developer.getPasswordHash() == null) {
            // 外部身份用户不能本地密码登录
            throw new BusinessException(ErrorCode.ACCOUNT_EXTERNAL_ONLY);
        }
        if (!PasswordHasher.verify(password, developer.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID);
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
        AuthResponseResult dto = new AuthResponseResult();
        dto.setToken(token);
        dto.setUserId(developer.getDeveloperId());
        dto.setUsername(developer.getUsername());
        dto.setUserType("developer");
        return dto;
    }

    @Override
    @Transactional
    public Optional<AuthResponseResult> handleExternalLogin(String providerName, String providerSubject, String email, String displayName, String rawInfoJson) {
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
            developer.setStatus(DeveloperStatus.APPROVED);
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
        AuthResponseResult dto = new AuthResponseResult();
        dto.setToken(token);
        dto.setUserId(developer.getDeveloperId());
        dto.setUsername(developer.getUsername());
//        dto.setStatus(developer.getStatus());
        dto.setUserType("developer");
        log.info("[handleExternalLogin] 返回JWT，developerId={}, token={}...", developer.getDeveloperId(), token != null ? token.substring(0, Math.min(16, token.length())) : null);
        return Optional.of(dto);
    }

    @Override
    public AuthResponseResult generateAuthResult(Developer developer) {
        // 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", developer.getDeveloperId());
        claims.put("userType", "developer");
        String token = jwtService.generateToken(
                "developer", // userType
                developer.getDeveloperId(), // userId
                claims // extraClaims
        );
        AuthResponseResult dto = new AuthResponseResult();
        dto.setToken(token);
        dto.setUserId(developer.getDeveloperId());
        dto.setUsername(developer.getUsername());
        dto.setUserType("developer");
        return dto;
    }

    @Override
    @Transactional
    public void bindExternalIdentity(String userId, String providerName, String providerSubject, String displayName, String rawInfoJson, String portalId) {
        // 通过portalId查询对应的Portal，然后获取PortalSetting
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();
        if (portalSetting == null) {
            log.error("[bindExternalIdentity] PortalSetting不存在，portalId={}", portalId);
            throw new BusinessException(ErrorCode.PORTAL_SETTING_NOT_FOUND);
        }

        // 查库校验 providerName 是否为有效 provider
        boolean valid = false;
        if (portalSetting.getOidcConfigs() != null) {
            for (com.alibaba.apiopenplatform.support.portal.OidcConfig c : portalSetting.getOidcConfigs()) {
                if (providerName.equals(c.getProvider()) && c.isEnabled()) {
                    valid = true;
                    break;
                }
            }
        }
        if (!valid) {
            throw new BusinessException(ErrorCode.OIDC_CONFIG_DISABLED);
        }
        log.info("[bindExternalIdentity] userId={}, portalId={}, providerName={}, providerSubject={}", userId, portalId, providerName, providerSubject);
        Optional<Developer> devOpt = findByDeveloperId(userId);
        log.info("[bindExternalIdentity] findByDeveloperId({}) result: {}", userId, devOpt.isPresent() ? devOpt.get().getUsername() : "not found");
        if (!devOpt.isPresent()) {
            log.error("[bindExternalIdentity] 用户不存在，无法绑定，userId={}", userId);
            throw new BusinessException(ErrorCode.DEVELOPER_NOT_FOUND, userId);
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
     *
     * @param userId          当前开发者ID
     * @param providerName    第三方类型
     * @param providerSubject 第三方唯一标识
     * @param portalId        门户唯一标识（建议前端传递）
     */
    @Transactional
    public void unbindExternalIdentity(String userId, String providerName, String providerSubject, String portalId) {
        // 通过portalId查询对应的Portal，然后获取PortalSetting
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();
        if (portalSetting == null) {
            log.error("[unbindExternalIdentity] PortalSetting不存在，portalId={}", portalId);
            throw new BusinessException(ErrorCode.PORTAL_SETTING_NOT_FOUND);
        }

        // 新增：查库校验 providerName 是否为有效 provider
        boolean valid = false;
        if (portalSetting.getOidcConfigs() != null) {
            for (com.alibaba.apiopenplatform.support.portal.OidcConfig c : portalSetting.getOidcConfigs()) {
                if (providerName.equals(c.getProvider()) && c.isEnabled()) {
                    valid = true;
                    break;
                }
            }
        }
        if (!valid) {
            throw new BusinessException(ErrorCode.OIDC_CONFIG_DISABLED);
        }
        // 查找该用户所有外部身份
        java.util.List<DeveloperExternalIdentity> identities = developerExternalIdentityRepository.findByDeveloper_DeveloperId(userId);
        // 查找开发者
        Developer developer = developerRepository.findByDeveloperId(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        boolean hasBuiltin = developer.getPasswordHash() != null;
        long otherCount = identities.stream()
                .filter(id -> !(id.getProvider().equals(providerName) && id.getSubject().equals(providerSubject)))
                .count();
        if (!hasBuiltin && otherCount == 0) {
            throw new BusinessException(ErrorCode.DEVELOPER_UNBIND_FAILED);
        }
        // 删除外部身份
        developerExternalIdentityRepository.deleteByProviderAndSubjectAndDeveloper_DeveloperId(providerName, providerSubject, userId);
        log.info("[unbindExternalIdentity] 解绑成功，userId={}, portalId={}, providerName={}, providerSubject={}", userId, portalId, providerName, providerSubject);
    }

    /**
     * 注销开发者账号（删除账号及所有外部身份）
     *
     * @param userId 当前开发者ID
     */
    @Transactional
    public void deleteDeveloperAccount(String userId) {
        eventPublisher.publishEvent(new DeveloperDeletingEvent(userId));

        // 删除所有外部身份
        developerExternalIdentityRepository.deleteByDeveloper_DeveloperId(userId);
        // 删除开发者主表
        developerRepository.findByDeveloperId(userId).ifPresent(developerRepository::delete);
        // TODO: 删除其他相关数据（如token、个人信息、日志等，视业务而定）
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

        return new PageResult<DeveloperResult>().convertFrom(
                developers,
                developer -> new DeveloperResult().convertFrom(developer)
        );
    }

    @Override
    public void setDeveloperStatus(String portalId, String developerId, String status) {
        portalService.hasPortal(portalId);
        Developer developer = findDeveloper(developerId);

        DeveloperStatus developerStatus;
        if ("APPROVED".equalsIgnoreCase(status)) {
            developerStatus = DeveloperStatus.APPROVED;
        } else {
            developerStatus = DeveloperStatus.PENDING;
        }
        developer.setStatus(developerStatus);
        developerRepository.save(developer);
    }

    private String generateDeveloperId() {
        return IdGenerator.genDeveloperId();
    }

    private Developer findDeveloper(String developerId) {
        return developerRepository.findByDeveloperId(developerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.DEVELOPER, developerId));
    }

    @Override
    @Transactional
    public boolean changePassword(String developerId, String oldPassword, String newPassword) {
        Optional<Developer> devOpt = developerRepository.findByDeveloperId(developerId);
        if (!devOpt.isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "developer", developerId);
        }
        Developer developer = devOpt.get();
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
        Optional<Developer> devOpt = developerRepository.findByDeveloperId(developerId);
        if (!devOpt.isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "developer", developerId);
        }
        Developer developer = devOpt.get();

        // 检查用户名唯一性（如果修改了用户名）
        if (username != null && !username.equals(developer.getUsername())) {
            if (developerRepository.findByUsername(username).isPresent()) {
                throw new BusinessException(ErrorCode.DEVELOPER_USERNAME_EXISTS, username);
            }
            developer.setUsername(username);
        }

        // 更新邮箱
        if (email != null) {
            developer.setEmail(email);
        }

        // 更新头像
        if (avatarUrl != null) {
            developer.setAvatarUrl(avatarUrl);
        }

        developerRepository.save(developer);
        return true;
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

    @EventListener
    @Async("taskExecutor")
    public void handlePortalDeletion(PortalDeletingEvent event) {
        String portalId = event.getPortalId();
        try {
            log.info("Starting to cleanup developers for portal {}", portalId);
            List<Developer> developers = developerRepository.findByPortalId(portalId);

            for (Developer developer : developers) {
                deleteDeveloperAccount(developer.getDeveloperId());
            }

            log.info("Completed cleanup of {} developers for portal {}", developers.size(), portalId);
        } catch (Exception e) {
            log.error("Failed to cleanup developers for portal {}: {}", portalId, e.getMessage());
        }
    }
} 