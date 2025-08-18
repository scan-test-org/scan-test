package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.aliyun.sdk.service.apig20240327.models.HttpApiApiInfo;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class APIResult implements OutputConverter<APIResult, HttpApiApiInfo> {

    private String apiId;

    private String apiName;

    @Override
    public APIResult convertFrom(HttpApiApiInfo apiInfo) {
        setApiId(apiInfo.getHttpApiId());
        setApiName(apiInfo.getName());
        return this;
    }
}
