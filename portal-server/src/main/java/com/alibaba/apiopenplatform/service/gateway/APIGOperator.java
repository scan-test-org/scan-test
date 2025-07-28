package com.alibaba.apiopenplatform.service.gateway;

import cn.hutool.core.codec.Base64;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.support.enums.APIGAPIType;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.entity.Gateway;
import com.alibaba.apiopenplatform.service.gateway.client.APIGClient;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.aliyun.sdk.service.apig20240327.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zh
 */
@RequiredArgsConstructor
@Service
@Slf4j
@Primary
public class APIGOperator extends GatewayOperator<APIGClient> {

    @Override
    public PageResult<APIResult> fetchHTTPAPIs(Gateway gateway, Pageable pageable) {
        return fetchAPIs(gateway, APIGAPIType.HTTP, pageable);
    }

    public PageResult<APIResult> fetchRESTAPIs(Gateway gateway, Pageable pageable) {
        return fetchAPIs(gateway, APIGAPIType.REST, pageable);
    }

    @Override
    public PageResult<? extends MCPServerResult> fetchMcpServers(Gateway gateway, Pageable pageable) {
        throw new UnsupportedOperationException("APIG does not support MCP Servers");
    }

    @Override
    public String fetchAPISpec(Gateway gateway, String apiId) {
        APIGClient client = getClient(gateway);

        try {
            ExportHttpApiResponse response = client.execute(c -> {
                ExportHttpApiRequest request = ExportHttpApiRequest.builder()
                        .httpApiId(apiId)
                        .build();
                try {
                    return c.exportHttpApi(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            if (response.getStatusCode() != 200) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }

            String contentBase64 = response.getBody().getData().getSpecContentBase64();

            return Base64.decodeStr(contentBase64);
        } catch (Exception e) {
            log.error("Error fetching API Spec", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching API Spec，Cause：" + e.getMessage());
        }
    }

    @Override
    public String fetchMcpSpec(Gateway gateway, String apiId, String routeIdentifier) {
        throw new UnsupportedOperationException("APIG does not support MCP Servers");
    }

    public PageResult<GatewayResult> fetchGateways(QueryAPIGParam param, Pageable pageable) {
        APIGClient client = new APIGClient(param.convertTo());

        List<GatewayResult> gateways = new ArrayList<>();
        try {
            ListGatewaysResponse response = client.execute(c -> {
                ListGatewaysRequest request = ListGatewaysRequest.builder()
                        .gatewayType(param.getGatewayType().getType())
                        .pageNumber(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .build();
                try {
                    return c.listGateways(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            if (response.getStatusCode() != 200) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }

            for (ListGatewaysResponseBody.Items item : response.getBody().getData().getItems()) {
                gateways.add(GatewayResult.builder()
                        .gatewayName(item.getName())
                        .gatewayId(item.getGatewayId())
                        .region(param.getRegion())
                        .gatewayType(param.getGatewayType())
                        .build());
            }

            int total = Math.toIntExact(response.getBody().getData().getTotalSize());
            return PageResult.of(gateways, pageable.getPageNumber(), pageable.getPageSize(), total);
        } catch (Exception e) {
            log.error("Error fetching Gateways", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching Gateways，Cause：" + e.getMessage());
        }
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

    public APIResult fetchAPI(Gateway gateway, String apiId) {
        APIGClient client = getClient(gateway);
        try {
            GetHttpApiResponse response = client.execute(c -> {
                GetHttpApiRequest request = GetHttpApiRequest.builder()
                        .httpApiId(apiId)
                        .build();
                try {
                    return c.getHttpApi(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            if (response.getStatusCode() != 200) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }

            HttpApiApiInfo apiInfo = response.getBody().getData();
            return new APIResult().convertFrom(apiInfo);
        } catch (Exception e) {
            log.error("Error fetching API", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching API，Cause：" + e.getMessage());
        }
    }

    protected HttpRoute fetchHTTPRoute(Gateway gateway, String apiId, String routeId) {
        APIGClient client = getClient(gateway);

        try {
            GetHttpApiRouteResponse response = client.execute(c -> {
                GetHttpApiRouteRequest request = GetHttpApiRouteRequest.builder()
                        .httpApiId(apiId)
                        .routeId(routeId)
                        .build();
                try {
                    return c.getHttpApiRoute(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            if (response.getStatusCode() != 200) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }

            return response.getBody().getData();

        } catch (Exception e) {
            log.error("Error fetching HTTP Route", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching HTTP Route，Cause：" + e.getMessage());
        }
    }

    protected PageResult<APIResult> fetchAPIs(Gateway gateway, APIGAPIType type, Pageable pageable) {
        APIGClient client = getClient(gateway);
        try {
            List<APIResult> apis = new ArrayList<>();
            ListHttpApisResponse response = client.execute(c -> {
                ListHttpApisRequest request = ListHttpApisRequest.builder()
                        .gatewayId(gateway.getGatewayId())
                        .gatewayType(gateway.getGatewayType().getType())
                        .types(type.getType())
                        .pageNumber(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .build();
                try {
                    return c.listHttpApis(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            if (response.getStatusCode() != 200) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }

            for (HttpApiInfoByName item : response.getBody().getData().getItems()) {
                for (HttpApiApiInfo apiInfo : item.getVersionedHttpApis()) {
                    APIResult apiResult = new APIResult().convertFrom(apiInfo);
                    apis.add(apiResult);
                    break;
                }
            }

            int total = response.getBody().getData().getTotalSize();
            return PageResult.of(apis, pageable.getPageNumber(), pageable.getPageSize(), total);
        } catch (Exception e) {
            log.error("Error fetching APIs", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching APIs，Cause：" + e.getMessage());
        }
    }

    public PageResult<HttpRoute> fetchHttpRoutes(Gateway gateway, String apiId, Pageable pageable) {
        APIGClient client = getClient(gateway);
        try {
            ListHttpApiRoutesResponse response = client.execute(c -> {
                ListHttpApiRoutesRequest request = ListHttpApiRoutesRequest.builder()
                        .gatewayId(gateway.getGatewayId())
                        .httpApiId(apiId)
                        .pageNumber(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .build();
                try {
                    return c.listHttpApiRoutes(request).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            if (response.getStatusCode() != 200) {
                throw new BusinessException(ErrorCode.GATEWAY_ERROR, response.getBody().getMessage());
            }
            List<HttpRoute> httpRoutes = response.getBody().getData().getItems();
            int total = response.getBody().getData().getTotalSize();
            return PageResult.of(httpRoutes, pageable.getPageNumber(), pageable.getPageSize(), total);
        } catch (Exception e) {
            log.error("Error fetching HTTP Roues", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Error fetching HTTP Roues，Cause：" + e.getMessage());
        }
    }
}


