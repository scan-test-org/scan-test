package com.alibaba.apiopenplatform.dto.params.portal;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.PortalDomain;
import com.alibaba.apiopenplatform.support.enums.DomainType;
import com.alibaba.apiopenplatform.support.enums.ProtocolType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zh
 */
@Data
public class BindDomainParam implements InputConverter<PortalDomain> {

    @NotBlank(message = "门户域名不能为空")
    private String domain;

    @NotNull(message = "域名协议不能为空")
    private ProtocolType protocol;

    private DomainType type = DomainType.CUSTOM;
}
