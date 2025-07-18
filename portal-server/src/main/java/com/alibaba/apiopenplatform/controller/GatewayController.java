package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.gateway.ImportGatewayParam;
import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.GatewayResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.service.GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zh
 */
@Tag(name = "网关资源管理")
@RestController
@RequiredArgsConstructor
public class GatewayController {

    private final GatewayService gatewayService;

    @Operation(summary = "获取Gateway列表")
    @GetMapping("/gateway/fetch")
    public PageResult<GatewayResult> fetchGateways(@RequestParam String accessKey,
                                                  @RequestParam String secretKey,
                                                  @RequestParam String region,
                                                  Pageable pageable) {
        return null;
    }

    @Operation(summary = "获取导入的Gateway列表")
    @GetMapping("/gateway/list")
    public PageResult<GatewayResult> listGateways(Pageable pageable) {
        return null;
    }

    @Operation(summary = "导入Gateway")
    @GetMapping("/gateway/import")
    public void importGateway(@RequestBody ImportGatewayParam param) {

    }

    @Operation(summary = "获取API列表")
    @GetMapping("/api/list")
    public PageResult<APIResult> listAPIs(@RequestParam String gatewayId,
                                          @RequestParam String apiType,
                                          Pageable pageable) {
        return gatewayService.fetchAPIs(gatewayId, apiType, pageable.getPageNumber(), pageable.getPageSize());
    }

    @Operation(summary = "获取MCP Server列表")
    @GetMapping("/mcp/list")
    public PageResult<MCPServerResult> listMcpServers(@RequestParam String gatewayId,
                                                      Pageable pageable) {
        return gatewayService.fetchMcpServers(gatewayId, pageable.getPageNumber(), pageable.getPageSize());
    }
}
