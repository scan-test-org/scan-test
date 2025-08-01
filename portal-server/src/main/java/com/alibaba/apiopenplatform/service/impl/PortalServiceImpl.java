package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.portal.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.entity.PortalDomain;
import com.alibaba.apiopenplatform.repository.PortalDomainRepository;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.support.enums.DomainType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    private final String domainFormat = "%s.api.portal.local";

    public PortalResult createPortal(CreatePortalParam param) {
        portalRepository.findByName(param.getName())
                .ifPresent(portal -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PORTAL, portal.getName());
                });

        String portalId = IdGenerator.genPortalId();
        Portal portal = param.convertTo();
        portal.setPortalId(portalId);
        portal.setAdminId("admin");

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
        portalRepository.saveAndFlush(portal);

        return getPortal(portal.getPortalId());
    }

//    @Override
//    public PortalResult updatePortalSetting(String portalId, UpdatePortalSettingParam param) {
//        Portal portal = findPortal(portalId);
//        PortalSetting portalSetting = portal.getPortalSetting();
//        param.update(portalSetting);
//
//        portalRepository.saveAndFlush(portal);
//        return getPortal(portal.getPortalId());
//    }

//    @Override
//    public PortalResult updatePortalUi(String portalId, UpdatePortalUiParam param) {
//        Portal portal = findPortal(portalId);
//
//        PortalUi portalUi = portal.getPortalUi();
//        Optional.ofNullable(param.getLogo())
//                .ifPresent(portalUi::setLogo);
//
//        Optional.ofNullable(param.getIcon())
//                .ifPresent(portalUi::setIcon);
//
//        portalRepository.saveAndFlush(portal);
//        return getPortal(portal.getPortalId());
//    }

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
    public PortalResult bindDomain(String portalId, BindDomainParam param) {
        Portal portal = findPortal(portalId);
        PortalDomain portalDomain = param.convertTo();
        portalDomain.setPortal(portal);
        portal.getPortalDomains().add(portalDomain);

        portalRepository.saveAndFlush(portal);
        return getPortal(portalId);
    }

    @Override
    public PortalResult unbindDomain(String portalId, String domain) {
        Portal portal = findPortal(portalId);
        PortalDomain portalDomain = portal.getPortalDomains().stream()
                .filter(pd -> StrUtil.equals(pd.getDomain(), domain))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL_DOMAIN, domain));

        // 默认域名不允许解绑
        if (portalDomain.getType() == DomainType.DEFAULT) {
            throw new BusinessException(ErrorCode.DOMAIN_NOT_ALLOWED_UNBIND, domain);
        }

        portal.getPortalDomains().remove(portalDomain);
        portalRepository.save(portal);
        return getPortal(portalId);
    }

//    @Override
//    public PortalSettingConfig getPortalSetting(String portalId) {
//        Portal portal = findPortal(portalId);
//        if (portal.getPortalSetting() == null) {
//            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, portalId + ": setting not found");
//        }
//        return new PortalSettingConfig().convertFrom(portal.getPortalSetting());
//    }

    private Portal findPortal(String portalId) {
        return portalRepository.findByPortalId(portalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, portalId));
    }
}
