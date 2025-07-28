package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import com.aliyun.sdk.service.apig20240327.models.HttpRouteMatch;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class APIGMCPServerResult extends MCPServerResult implements OutputConverter<APIGMCPServerResult, HttpRoute> {

    private String apiId;

    private String routeId;

    private String name;

    private List<HttpRoute.DomainInfos> domainInfos;

    private HttpRouteMatch match;

    private HttpRoute.McpServerInfo mcpServerInfo;
}
