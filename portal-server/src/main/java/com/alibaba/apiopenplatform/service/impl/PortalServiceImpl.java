package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.portal.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.entity.PortalUi;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.service.PortalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author zh
 */
@Service
@Slf4j
public class PortalServiceImpl implements PortalService {

    private final PortalRepository portalRepository;

    public PortalServiceImpl(PortalRepository portalRepository) {
        this.portalRepository = portalRepository;
    }

    public PortalResult createPortal(CreatePortalParam param) {
        if (portalRepository.findByNameAndAdminId(param.getName(), param.getAdminId()).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PORTAL, param.getName());
        }

        String portalId = IdGenerator.genPortalId();
        Portal portal = param.convertTo();
        portal.setPortalId(portalId);

        // 初始化
        PortalSetting portalSetting = new PortalSetting();
        portalSetting.setPortalId(portalId);
        PortalUi portalUi = new PortalUi();
        portalUi.setPortalId(portalId);
        portal.setPortalSetting(portalSetting);
        portal.setPortalUi(portalUi);

        portalRepository.save(portal);

        return getPortal(portalId);
    }

    @Override
    public PortalResult getPortal(String portalId) {
        Portal portal = findPortal(portalId);
        return new PortalResult().convertFrom(portal);
    }

    @Override
    public PageResult<PortalResult> listPortals(int pageNumber, int pageSize) {
        Page<Portal> portals = portalRepository.findByAdminId(
                "admin",
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Order.desc("gmtCreate")))
        );

        Page<PortalResult> pages = portals.map(portal -> new PortalResult().convertFrom(portal));
        return new PageResult<PortalResult>().convertFrom(pages);
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
            Optional.ofNullable(param.getOidcConfigParam())
                    .map(OidcConfigParam :: convertTo)
                    .ifPresent(portalSetting::setOidcConfig);
        }

        Optional.ofNullable(param.getAutoApproveDevelopers())
                .ifPresent(portalSetting::setAutoApproveDevelopers);

        Optional.ofNullable(param.getAutoApproveSubscriptions())
                .ifPresent(portalSetting::setAutoApproveSubscriptions);

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

    private Portal findPortal(String portalId) {
        return portalRepository.
                findByPortalIdAndAdminId(portalId,"admin")
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, portalId));
    }
}
