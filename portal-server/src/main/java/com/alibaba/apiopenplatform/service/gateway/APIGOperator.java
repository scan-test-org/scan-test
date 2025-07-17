package com.alibaba.apiopenplatform.service.gateway;

import com.alibaba.apiopenplatform.support.enums.APIGAPIType;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.service.gateway.client.APIGClient;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.gateway.APIGConfig;
import com.aliyun.sdk.service.apig20240327.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author zh
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class APIGOperator extends GatewayOperator<APIGClient> {

    @Override
    public PageResult<APIResult> fetchHTTPAPIs(Gateway gateway, int pageNumber, int pageSize) {
        return fetchAPIs(gateway, APIGAPIType.HTTP, pageNumber, pageSize);
    }

    public PageResult<APIResult> fetchRESTAPIs(Gateway gateway, int pageNumber, int pageSize) {
        return fetchAPIs(gateway, APIGAPIType.REST, pageNumber, pageSize);
    }

    @Override
    public PageResult<MCPServerResult> fetchMcpServers(Gateway gateway, int pageNumber, int pageSize) {
        throw new UnsupportedOperationException("APIG does not support MCP Servers");
    }

    @Override
    public void fetchAPISpec(Gateway gateway, String apiId) {

    }

    public void createConsumer(Gateway gateway) {
//        client.execute(a -> a.(null));

//        client.execute(c -> {})
    }

    @Override
    public void deleteConsumer(Gateway gateway) {

    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.APIG_API;
    }

    protected PageResult<APIResult> fetchAPIs(Gateway gateway, APIGAPIType type, int pageNumber, int pageSize) {
        APIGClient client = getClient(gateway);
        try {
            List<APIResult> apis = new ArrayList<>();
            ListHttpApisResponse response = client.execute(c -> {
                ListHttpApisRequest request = ListHttpApisRequest.builder()
                        .gatewayId(gateway.getGatewayId())
                        .types(type.getType())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .build();
                try {
                    return c.listHttpApis(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            for (HttpApiInfoByName item : response.getBody().getData().getItems()) {
                for (HttpApiApiInfo apiInfo : item.getVersionedHttpApis()) {
                    APIResult apiResult = new APIResult().convertFrom(apiInfo);
                    apis.add(apiResult);
                    break;
                }
            }

            int total = response.getBody().getData().getTotalSize();
            return PageResult.of(apis, pageNumber, pageSize, total);
        } catch (Exception e) {
            log.error("Error fetching APIs", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching APIs，Cause：" + e.getMessage());
        }
    }

    public PageResult<HttpRoute> fetchHttpRoutes(Gateway gateway, String apiId, int pageNumber, int pageSize) {
        APIGClient client = getClient(gateway);
        try {
            ListHttpApiRoutesResponse response = client.execute(c -> {
                ListHttpApiRoutesRequest request = ListHttpApiRoutesRequest.builder()
                        .gatewayId(gateway.getGatewayId())
                        .httpApiId(apiId)
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .build();
                try {
                    return c.listHttpApiRoutes(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            if (!"200".equals(response.getBody().getCode())) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }
            List<HttpRoute> httpRoutes = response.getBody().getData().getItems();
            int total = response.getBody().getData().getTotalSize();
            return PageResult.of(httpRoutes, pageNumber, pageSize, total);
        } catch (Exception e) {
            log.error("Error fetching HTTP Roues", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching HTTP Roues，Cause：" + e.getMessage());
        }
    }


    public static void main(String[] args) {
        Gateway gateway = new Gateway();
        gateway.setGatewayType(GatewayType.APIG_API);
        gateway.setGatewayId("hr-ct0423um1hkplhlihkbg");
        APIGConfig apigConfig = new APIGConfig();
        apigConfig.setAccessKey("LTAIIWKi2kwOxxCH");
        apigConfig.setSecretKey("");
        apigConfig.setRegion("cn-hangzhou");
        apigConfig.setGatewayId("hr-ct0423um1hkplhlihkbg");
        gateway.setApigConfig(apigConfig);

        APIGClient apigClient = new APIGClient(gateway);


        ListHttpApisRequest request = ListHttpApisRequest.builder()
                .gatewayId(gateway.getGatewayId())
                .types("Wet")
                .pageNumber(1)
                .pageSize(10)
                .build();

        CompletableFuture<ListHttpApisResponse> future = apigClient.execute(c -> c.listHttpApis(request));

        try {
            ListHttpApisResponse response = future.get();
            System.out.println(response.getBody().getData().getItems());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
