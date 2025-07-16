package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.portal.CreatePortalParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalSettingParam;
import com.alibaba.apiopenplatform.dto.params.portal.UpdatePortalUiParam;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import org.springframework.data.domain.Pageable;

/**
 * @author zh
 */
public interface PortalService {

    /**
     * 创建门户
     *
     * @param param
     * @return
     */
    PortalResult createPortal(CreatePortalParam param);

    /**
     * 查询门户
     *
     * @param portalId
     * @return
     */
    PortalResult getPortal(String portalId);

    /**
     * 检查门户是否存在
     *
     * @param portalId
     * @return
     */
    boolean hasPortal(String portalId);

    /**
     * 查询门户列表
     *
     * @param pageable
     * @return
     */
    PageResult<PortalResult> listPortals(Pageable pageable);

    /**
     * 更新门户
     *
     * @param param
     * @return
     */
    PortalResult updatePortal(UpdatePortalParam param);

    /**
     * 更新门户设置
     *
     * @param param
     * @return
     */
    PortalResult updatePortalSetting(UpdatePortalSettingParam param);

    /**
     * 更新门户UI
     *
     * @param param
     * @return
     */
    PortalResult updatePortalUi(UpdatePortalUiParam param);

    /**
     * 删除门户
     *
     * @param portalId
     */
    void deletePortal(String portalId);
}
