package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.PortalDomain;
import com.alibaba.apiopenplatform.support.enums.DomainType;
import com.alibaba.apiopenplatform.support.enums.ProtocolType;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class PortalDomainConfig implements OutputConverter<PortalDomainConfig, PortalDomain> {

    private String domain;

    private DomainType type;

    private ProtocolType protocol = ProtocolType.HTTP;
}
