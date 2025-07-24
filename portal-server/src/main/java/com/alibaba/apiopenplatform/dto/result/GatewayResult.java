package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author zh
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayResult implements OutputConverter<GatewayResult, Gateway> {

    private String gatewayId;

    private GatewayType gatewayType;

    private String gatewayName;

    private String region;

    private LocalDateTime createAt;

    @Override
    public GatewayResult convertFrom(Gateway source) {
        OutputConverter.super.convertFrom(source);
        if (source.getGatewayType().isAPIG()) {
            setRegion(source.getApigConfig().getRegion());
        }
        return this;
    }
}
