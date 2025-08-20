package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.gateway.APIGConfig;
import com.alibaba.apiopenplatform.support.gateway.HigressConfig;
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

    private APIGConfigResult apigConfig;

    private HigressConfigResult higressConfig;

    private LocalDateTime createAt;

    @Override
    public GatewayResult convertFrom(Gateway source) {
        OutputConverter.super.convertFrom(source);
        if (source.getGatewayType().isAPIG()) {
            setApigConfig(new APIGConfigResult().convertFrom(source.getApigConfig()));
        } else {
            setHigressConfig(new HigressConfigResult().convertFrom(source.getHigressConfig()));
        }
        return this;
    }

    @Data
    public static class APIGConfigResult implements OutputConverter<APIGConfigResult, APIGConfig> {
        private String region;
    }

    @Data
    public static class HigressConfigResult implements OutputConverter<HigressConfigResult, HigressConfig> {
        private String address;
        private String username;
    }
}
