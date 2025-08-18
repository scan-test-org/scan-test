package com.alibaba.apiopenplatform.dto.params.portal;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import com.alibaba.apiopenplatform.support.portal.PortalUiConfig;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author zh
 */
@Data
public class UpdatePortalParam implements InputConverter<Portal> {

    @Size(max = 50, message = "门户名称长度不能超过50个字符")
    private String name;

    @Size(max = 1024, message = "门户描述长度不能超过1024个字符")
    private String description;

    private PortalSettingConfig portalSettingConfig;

    private PortalUiConfig portalUiConfig;
}
