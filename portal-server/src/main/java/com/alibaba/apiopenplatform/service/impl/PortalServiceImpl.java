package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.event.PortalDeletingEvent;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.consumer.QuerySubscriptionParam;
import com.alibaba.apiopenplatform.dto.params.portal.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.dto.result.SubscriptionResult;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.entity.PortalDomain;
import com.alibaba.apiopenplatform.entity.ProductSubscription;
import com.alibaba.apiopenplatform.repository.PortalDomainRepository;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.repository.SubscriptionRepository;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.support.enums.DomainType;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import com.alibaba.apiopenplatform.support.portal.PortalUiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zh
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PortalServiceImpl implements PortalService {

    private final PortalRepository portalRepository;

    private final PortalDomainRepository portalDomainRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final SubscriptionRepository subscriptionRepository;

    private final ContextHolder contextHolder;

    private final String domainFormat = "%s.api.portal.local";

    public PortalResult createPortal(CreatePortalParam param) {
        portalRepository.findByName(param.getName())
                .ifPresent(portal -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PORTAL, portal.getName());
                });

        String portalId = IdGenerator.genPortalId();
        Portal portal = param.convertTo();
        portal.setPortalId(portalId);
        portal.setAdminId(contextHolder.getUser());

        // Setting & Ui
        portal.setPortalSettingConfig(new PortalSettingConfig());
        portal.setPortalUiConfig(new PortalUiConfig());

        // Domain
        PortalDomain portalDomain = new PortalDomain();
        portalDomain.setDomain(String.format(domainFormat, portalId));
        portalDomain.setPortalId(portalId);
        portal.getPortalDomains().add(portalDomain);

        portalRepository.save(portal);

        return getPortal(portalId);
    }

    @Override
    public PortalResult getPortal(String portalId) {
        Portal portal = findPortal(portalId);
        return new PortalResult().convertFrom(portal);
    }

    @Override
    public void existsPortal(String portalId) {
        portalRepository.findByPortalId(portalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, portalId));
    }

    @Override
    public PageResult<PortalResult> listPortals(Pageable pageable) {
        Page<Portal> portals = portalRepository.findAll(pageable);

        return new PageResult<PortalResult>().convertFrom(portals, portal -> new PortalResult().convertFrom(portal));
    }

    @Override
    public PortalResult updatePortal(String portalId, UpdatePortalParam param) {
        Portal portal = findPortal(portalId);

        Optional.ofNullable(param.getName())
                .filter(name -> !name.equals(portal.getName()))
                .flatMap(portalRepository::findByName)
                .ifPresent(p -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PORTAL, p.getName());
                });

        param.update(portal);
        // 至少保留一种认证方式
        PortalSettingConfig setting = portal.getPortalSettingConfig();
        if (BooleanUtil.isFalse(setting.getBuiltinAuthEnabled())) {
            boolean enabledOidc = Optional.ofNullable(setting.getOidcConfigs())
                    .filter(CollUtil::isNotEmpty)
                    .map(configs -> configs.stream().anyMatch(OidcConfig::isEnabled))
                    .orElse(false);

            if (!enabledOidc) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_OPERATION, "至少配置一种认证方式");
            }
        }
        portalRepository.saveAndFlush(portal);

        return getPortal(portal.getPortalId());
    }

    @Override
    public void deletePortal(String portalId) {
        Portal portal = findPortal(portalId);

        // 异步清理门户资源
        eventPublisher.publishEvent(new PortalDeletingEvent(portalId));
        portalRepository.delete(portal);
    }

    @Override
    public String resolvePortal(String domain) {
        return portalDomainRepository.findByDomain(domain)
                .map(PortalDomain::getPortalId)
                .orElse(null);
    }

    @Override
    public PortalResult bindDomain(String portalId, BindDomainParam param) {
        Portal portal = findPortal(portalId);
        portalDomainRepository.findByPortalIdAndDomain(portalId, param.getDomain())
                .ifPresent(portalDomain -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PORTAL_DOMAIN, param.getDomain());
                });

        PortalDomain portalDomain = param.convertTo();
        portalDomain.setPortalId(portalId);
        portal.getPortalDomains().add(portalDomain);

        portalRepository.saveAndFlush(portal);
        return getPortal(portalId);
    }

    @Override
    public PortalResult unbindDomain(String portalId, String domain) {
        portalDomainRepository.findByPortalIdAndDomain(portalId, domain)
                .ifPresent(portalDomain -> {
                    // 默认域名不允许解绑
                    if (portalDomain.getType() == DomainType.DEFAULT) {
                        throw new BusinessException(ErrorCode.UNSUPPORTED_OPERATION, "默认域名不允许解绑");
                    }
                    portalDomainRepository.delete(portalDomain);
                });
        return getPortal(portalId);
    }

    @Override
    public PageResult<SubscriptionResult> listSubscriptions(String portalId, QuerySubscriptionParam param, Pageable pageable) {
        // Ensure portal exists
        findPortal(portalId);

        Specification<ProductSubscription> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("portalId"), portalId));
            if (param != null && param.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), param.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductSubscription> page = subscriptionRepository.findAll(spec, pageable);
        return new PageResult<SubscriptionResult>().convertFrom(page, s -> new SubscriptionResult().convertFrom(s));
    }

    private Portal findPortal(String portalId) {
        return portalRepository.findByPortalId(portalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, portalId));
    }
}
