package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zh
 */
@Data
public class GatewayResult {

    private String gatewayId;

    private GatewayType gatewayType;

    private String gatewayName;

    private String region;

    private LocalDateTime gmtCreate;
}
