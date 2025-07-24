package com.alibaba.apiopenplatform.dto.params.gateway;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.support.gateway.HigressConfig;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

/**
 * @author zh
 */
@Data
public class HigressOption implements InputConverter<HigressConfig> {

    @NotBlank(message = "Higress Controller Host不能为空")
    private String host;

    @Range(min = 1, max = 65535, message = "端口号必须在1-65535之间")
    private int port;

    private String accessToken;

    private String jwtPolicy;
}
