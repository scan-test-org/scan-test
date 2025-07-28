package com.alibaba.apiopenplatform.service.gateway;

import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.APIGAPIType;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author zh
 */
@Service
@Slf4j
public class AIGatewayOperator extends APIGOperator {

    @Override
    public PageResult<? extends MCPServerResult> fetchMcpServers(Gateway gateway, Pageable pageable) {
        PageResult<APIResult> page = fetchAPIs(gateway, APIGAPIType.MCP, PageRequest.of(0, 1));
        if (page.getTotalElements() == 0) {
            return PageResult.empty(pageable.getPageNumber(), pageable.getPageSize());
        }

        // MCP Server定义在一个API下
        String apiId = page.getContent().get(0).getApiId();
        try {
            PageResult<HttpRoute> routesPage = fetchHttpRoutes(gateway, apiId, pageable);
            if (routesPage.getTotalElements() == 0) {
                return PageResult.empty(pageable.getPageNumber(), pageable.getPageSize());
            }

            return PageResult.<APIGMCPServerResult>builder().build()
                    .mapFrom(routesPage, route -> {
                        APIGMCPServerResult r = new APIGMCPServerResult().convertFrom(route);
                        r.setApiId(apiId);
                        return r;
                    });
        } catch (Exception e) {
            log.error("Error fetching MCP servers", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching MCP servers，Cause：" + e.getMessage());
        }
    }

    @Override
    public String fetchMcpSpec(Gateway gateway, String apiId, String routeId, String name) {
        HttpRoute httpRoute = fetchHTTPRoute(gateway, apiId, routeId);

        APIGMCPServerResult mcpServerResult = new APIGMCPServerResult().convertFrom(httpRoute);
        return JSONUtil.toJsonStr(mcpServerResult);
    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.APIG_AI;
    }
}
