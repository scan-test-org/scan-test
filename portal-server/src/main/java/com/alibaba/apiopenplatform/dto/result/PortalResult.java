package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Portal;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class PortalResult implements OutputConverter<PortalResult, Portal> {

    private String portalId;

    private String name;

    private String description;

    private String adminId;

    private PortalSettingConfig portalSettingConfig;

    private PortalUiConfig portalUiConfig;
}
