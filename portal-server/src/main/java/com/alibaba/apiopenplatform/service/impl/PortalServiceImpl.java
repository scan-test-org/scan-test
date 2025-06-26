package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.core.constant.ResourceNameConstants;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.PortalParam;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.dto.result.PortalSettingConfig;
import com.alibaba.apiopenplatform.dto.result.PortalUiConfig;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.entity.PortalUi;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.repository.PortalSettingRepository;
import com.alibaba.apiopenplatform.repository.PortalUiRepository;
import com.alibaba.apiopenplatform.service.PortalService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author zh
 */
@Service
public class PortalServiceImpl implements PortalService {

    private final PortalRepository portalRepository;

    private final PortalUiRepository portalUiRepository;

    private final PortalSettingRepository portalSettingRepository;

    public PortalServiceImpl(PortalRepository portalRepository,
                             PortalUiRepository portalUiRepository,
                             PortalSettingRepository portalSettingRepository) {
        this.portalRepository = portalRepository;
        this.portalUiRepository = portalUiRepository;
        this.portalSettingRepository = portalSettingRepository;
    }

    public PortalResult createPortal(PortalParam portalParam) {
        Optional<Portal> found = portalRepository.findByNameAndAdminId(portalParam.getName(), portalParam.getAdminId());
        if (found.isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, ResourceNameConstants.PORTAL, portalParam.getName());
        }

        // TODO 校验管理员

        Portal portal = portalParam.convertTo();
        String portalId = IdGenerator.genPortalId();
        portal.setPortalId(portalId);

        // Default Config
        PortalSetting portalSetting = new PortalSetting();
        portalSetting.setPortalId(portalId);
        PortalUi portalUi = new PortalUi();
        portalUi.setPortalId(portalId);

        // Save
        portalRepository.save(portal);
        portalSettingRepository.save(portalSetting);
        portalUiRepository.save(portalUi);

        return buildPortalResult(portal, portalSetting, portalUi);
    }


    private PortalResult buildPortalResult(Portal portal,
                                           PortalSetting portalSetting,
                                           PortalUi portalUi) {
        PortalResult portalResult = new PortalResult().convertFrom(portal);
        PortalSettingConfig portalSettingConfig = new PortalSettingConfig().convertFrom(portalSetting);
        PortalUiConfig portalUiConfig = new PortalUiConfig().convertFrom(portalUi);

        portalResult.setPortalSettingConfig(portalSettingConfig);
        portalResult.setPortalUiConfig(portalUiConfig);

        return portalResult;
    }
}
