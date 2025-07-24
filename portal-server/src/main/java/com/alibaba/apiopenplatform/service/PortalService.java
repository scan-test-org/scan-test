package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.portal.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.dto.result.PortalSettingConfig;
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
     * @param portalId
     * @param param
     * @return
     */
    PortalResult updatePortal(String portalId, UpdatePortalParam param);

    /**
     * 更新门户设置
     *
     * @param portalId
     * @param param
     * @return
     */
    PortalResult updatePortalSetting(String portalId, UpdatePortalSettingParam param);

    /**
     * 更新门户UI
     *
     * @param portalId
     * @param param
     * @return
     */
    PortalResult updatePortalUi(String portalId, UpdatePortalUiParam param);

    /**
     * 删除门户
     *
     * @param portalId
     */
    void deletePortal(String portalId);

    /**
     * 获取门户配置信息（PortalSetting）
     *
     * @param portalId
     * @return
     */
    PortalSettingConfig getPortalSetting(String portalId);

    /**
     * 根据请求域名解析门户
     *
     * @param domain
     * @return
     */
    String resolvePortal(String domain);


    /**
     * 为门户绑定域名
     *
     * @param portalId
     * @param param
     * @return
     */
    PortalResult bindDomain(String portalId, BindDomainParam param);

    /**
     * 删除门户绑定域名
     *
     * @param portalId
     * @param domain
     * @return
     */
    PortalResult unbindDomain(String portalId, String domain);
}
