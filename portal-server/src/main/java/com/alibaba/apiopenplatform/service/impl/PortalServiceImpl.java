package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.portal.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.dto.result.PortalSettingConfig;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.entity.PortalDomain;
import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.entity.PortalUi;
import com.alibaba.apiopenplatform.repository.PortalDomainRepository;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    private final String domainFormat = "%s.api.portal.com";

    public PortalResult createPortal(CreatePortalParam param) {
        portalRepository.findByNameAndAdminId(param.getName(), "admin")
                .ifPresent(portal -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PORTAL, portal.getName());
                });

        String portalId = IdGenerator.genPortalId();
        Portal portal = param.convertTo();
        portal.setPortalId(portalId);
        portal.setAdminId("admin");

        // Setting
        PortalSetting portalSetting = new PortalSetting();
        portalSetting.setPortal(portal);
        // Ui
        PortalUi portalUi = new PortalUi();
        portalUi.setPortal(portal);
        portal.setPortalSetting(portalSetting);
        portal.setPortalUi(portalUi);
        // Domain
        PortalDomain portalDomain = new PortalDomain();
        portalDomain.setDomain(String.format(domainFormat, portalId));
        portalDomain.setPortal(portal);
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
    public boolean hasPortal(String portalId) {
        return findPortal(portalId) != null;
    }

    @Override
    public PageResult<PortalResult> listPortals(Pageable pageable) {
        Page<Portal> portals = portalRepository.findByAdminId("admin", pageable);

        return new PageResult<PortalResult>().convertFrom(portals, portal -> new PortalResult().convertFrom(portal));
    }

    @Override
    public PortalResult updatePortal(UpdatePortalParam param) {
        Portal portal = findPortal(param.getPortalId());

        param.update(portal);
        portalRepository.saveAndFlush(portal);

        return getPortal(portal.getPortalId());
    }

    @Override
    public PortalResult updatePortalSetting(UpdatePortalSettingParam param) {
        Portal portal = findPortal(param.getPortalId());
        PortalSetting portalSetting = portal.getPortalSetting();
        Optional.ofNullable(param.getBuiltinAuthEnabled())
                .ifPresent(portalSetting::setBuiltinAuthEnabled);
        Optional.ofNullable(param.getOidcAuthEnabled())
                .ifPresent(portalSetting::setOidcAuthEnabled);

        // OIDC
        if (Boolean.TRUE.equals(param.getOidcAuthEnabled())) {
            Optional.ofNullable(param.getOidcOption())
                    .map(OidcOption::convertTo)
                    .ifPresent(portalSetting::setOidcConfig);
        }

        Optional.ofNullable(param.getAutoApproveDevelopers())
                .ifPresent(portalSetting::setAutoApproveDevelopers);
        Optional.ofNullable(param.getAutoApproveSubscriptions())
                .ifPresent(portalSetting::setAutoApproveSubscriptions);
        Optional.ofNullable(param.getFrontendRedirectUrl())
                .ifPresent(portalSetting::setFrontendRedirectUrl);
        // 批量配置OIDC provider
        if (param.getOidcConfigParams() != null) {
            List<OidcConfig> configs = new java.util.ArrayList<>();
            for (OidcConfigParam p : param.getOidcConfigParams()) {
                OidcConfig config = p.convertTo();
                config.setProvider(p.getProvider());
                configs.add(config);
            }
            portalSetting.setOidcConfigs(configs);
        }
        portalRepository.saveAndFlush(portal);
        return getPortal(portal.getPortalId());
    }

    @Override
    public PortalResult updatePortalUi(UpdatePortalUiParam param) {
        Portal portal = findPortal(param.getPortalId());

        PortalUi portalUi = portal.getPortalUi();
        Optional.ofNullable(param.getLogo())
                .ifPresent(portalUi::setLogo);

        Optional.ofNullable(param.getIcon())
                .ifPresent(portalUi::setIcon);

        portalRepository.saveAndFlush(portal);
        return getPortal(portal.getPortalId());
    }

    @Override
    public void deletePortal(String portalId) {
        Portal portal = findPortal(portalId);
        portalRepository.delete(portal);
    }

    @Override
    public String resolvePortal(String domain) {
        return portalDomainRepository.findByDomain(domain)
                .map(portalDomain -> portalDomain.getPortal().getPortalId())
                .orElse(null);
    }

    @Override
    public PortalSettingConfig getPortalSetting(String portalId) {
        Portal portal = findPortal(portalId);
        if (portal.getPortalSetting() == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, portalId + ": setting not found");
        }
        return new PortalSettingConfig().convertFrom(portal.getPortalSetting());
    }

    private Portal findPortal(String portalId) {
        return portalRepository.findByPortalId(portalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, portalId));
    }
}
