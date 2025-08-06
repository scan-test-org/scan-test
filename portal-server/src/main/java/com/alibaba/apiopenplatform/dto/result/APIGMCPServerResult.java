package com.alibaba.apiopenplatform.dto.result;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import com.aliyun.sdk.service.apig20240327.models.HttpRouteMatch;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class APIGMCPServerResult extends GatewayMCPServerResult implements OutputConverter<APIGMCPServerResult, HttpRoute> {

    private String apiId;

    private String mcpRouteId;

    @Override
    public APIGMCPServerResult convertFrom(HttpRoute httpRoute) {
        APIGMCPServerResult r = OutputConverter.super.convertFrom(httpRoute);
        r.setMcpServerName(httpRoute.getName());
        r.setMcpRouteId(httpRoute.getRouteId());
        return r;
    }
}
