package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.support.enums.APIGAPIType;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zh
 */
@Service
@Slf4j
public class AIGatewayOperator extends APIGOperator {

    @Override
    public PageResult<MCPServerResult> fetchMcpServers(Gateway gateway, int pageNumber, int pageSize) {
        PageResult<APIResult> page = fetchAPIs(gateway, APIGAPIType.MCP, 1, 1);
        if (page.getTotalElements() == 0) {
            return PageResult.empty(pageNumber, pageSize);
        }

        // MCP Server定义在一个API下
        String apiId = page.getContent().get(0).getApiId();
        try {
            PageResult<HttpRoute> routesPage = fetchHttpRoutes(gateway, apiId, pageNumber, pageSize);
            if (routesPage.getTotalElements() == 0) {
                return PageResult.empty(pageNumber, pageSize);
            }

            return PageResult.<MCPServerResult>builder().build()
                    .mapFrom(routesPage, new MCPServerResult()::convertFrom);
        } catch (Exception e) {
            log.error("Error fetching MCP servers", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching MCP servers，Cause：" + e.getMessage());
        }
    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.APIG_AI;
    }
}
