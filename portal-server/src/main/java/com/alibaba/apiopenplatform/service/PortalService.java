package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.PortalParam;
import com.alibaba.apiopenplatform.dto.result.PortalResult;

/**
 * @author zh
 */
public interface PortalService {

    /**
     * 创建门户
     *
     * @param portalParam
     * @return
     */
    PortalResult createPortal(PortalParam portalParam);
}
