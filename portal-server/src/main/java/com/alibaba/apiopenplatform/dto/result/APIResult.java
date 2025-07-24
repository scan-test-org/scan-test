package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.aliyun.sdk.service.apig20240327.models.HttpApiApiInfo;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class APIResult implements OutputConverter<APIResult, HttpApiApiInfo> {

    private String apiId;

    private String name;

    private String type;

    @Override
    public APIResult convertFrom(HttpApiApiInfo apiInfo) {
        setApiId(apiInfo.getHttpApiId());
        setName(apiInfo.getName());
        setType(apiInfo.getType());
        return this;
    }
}
