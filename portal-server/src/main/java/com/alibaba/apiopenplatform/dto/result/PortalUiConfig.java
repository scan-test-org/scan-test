package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.PortalUi;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class PortalUiConfig implements OutputConverter<PortalUiConfig, PortalUi> {

    private String logo;

    private String icon;
}
