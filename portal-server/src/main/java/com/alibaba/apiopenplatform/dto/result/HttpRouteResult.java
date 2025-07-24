package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;

import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import com.aliyun.sdk.service.apig20240327.models.HttpRouteMatch;
import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class HttpRouteResult implements OutputConverter<HttpRouteResult, HttpRoute> {

    private String name;

    private List<HttpRoute.DomainInfos> domainInfos;

    private HttpRouteMatch match;

    private HttpRoute.McpServerInfo mcpServerInfo;
}

