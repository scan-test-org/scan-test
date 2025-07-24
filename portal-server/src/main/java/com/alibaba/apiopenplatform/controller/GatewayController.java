package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.annotation.AdminAuth;
import com.alibaba.apiopenplatform.dto.params.gateway.ImportGatewayParam;
import com.alibaba.apiopenplatform.dto.params.gateway.QueryAPIGParam;
import com.alibaba.apiopenplatform.dto.result.APIResult;
import com.alibaba.apiopenplatform.dto.result.GatewayResult;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.service.GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * @author zh
 */
@Tag(name = "网关资源管理")
@RestController
@RequestMapping
@RequiredArgsConstructor
@AdminAuth
public class GatewayController {

    private final GatewayService gatewayService;

    @Operation(summary = "获取APIG Gateway列表")
    @GetMapping("/gateways/fetch")
    public PageResult<GatewayResult> fetchGateways(@Valid QueryAPIGParam param,
                                                   Pageable pageable) {
        return gatewayService.fetchGateways(param, pageable);
    }

    @Operation(summary = "获取导入的Gateway列表")
    @GetMapping("/gateways/list")
    public PageResult<GatewayResult> listGateways(Pageable pageable) {
        return gatewayService.listGateways(pageable);
    }

    @Operation(summary = "导入Gateway")
    @PostMapping("/gateways/import")
    public void importGateway(@RequestBody @Valid ImportGatewayParam param) {
        gatewayService.importGateway(param);
    }

    @Operation(summary = "删除Gateway")
    @DeleteMapping("/gateways/{gatewayId}")
    public void deleteGateway(@PathVariable String gatewayId) {
        gatewayService.deleteGateway(gatewayId);
    }

    @Operation(summary = "获取API列表")
    @GetMapping("/apis/fetch")
    public PageResult<APIResult> fetchAPIs(@RequestParam String gatewayId,
                                           @RequestParam String apiType,
                                           Pageable pageable) {
        return gatewayService.fetchAPIs(gatewayId, apiType, pageable);
    }

    @Operation(summary = "获取MCP Server列表")
    @GetMapping("/mcp-servers/fetch")
    public PageResult<MCPServerResult> fetchMcpServers(@RequestParam String gatewayId,
                                                       Pageable pageable) {
        return gatewayService.fetchMcpServers(gatewayId, pageable);
    }
}
