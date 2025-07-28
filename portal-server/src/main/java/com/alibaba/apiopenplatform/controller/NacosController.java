package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.annotation.AdminAuth;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketCardParam;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketDetailParam;
import com.alibaba.apiopenplatform.dto.params.nacos.CreateNacosParam;
import com.alibaba.apiopenplatform.dto.params.nacos.UpdateNacosParam;
import com.alibaba.apiopenplatform.dto.result.MCPServerResult;
import com.alibaba.apiopenplatform.dto.result.NacosResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.service.NacosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Nacos实例管理与能力市场统一控制器
 * @author zxd
 */
@Tag(name = "Nacos资源管理")
@RestController
@RequestMapping("/nacos")
@RequiredArgsConstructor
@AdminAuth
public class NacosController {

    private final NacosService nacosService;

    // ----------- Nacos实例管理相关 -----------
    @Operation(summary = "获取Nacos实例列表")
    @GetMapping
    public PageResult<NacosResult> listNacosInstances(Pageable pageable) {
        return nacosService.listNacosInstances(pageable);
    }

    @Operation(summary = "创建Nacos实例")
    @PostMapping
    public void createNacosInstance(@RequestBody @Valid CreateNacosParam param) {
        nacosService.createNacosInstance(param);
    }

    @Operation(summary = "更新Nacos实例")
    @PutMapping("/{nacosId}")
    public void updateNacosInstance(@PathVariable String nacosId, @RequestBody @Valid UpdateNacosParam param) {
        nacosService.updateNacosInstance(nacosId, param);
    }

    @Operation(summary = "删除Nacos实例")
    @DeleteMapping("/{nacosId}")
    public void deleteNacosInstance(@PathVariable String nacosId) {
        nacosService.deleteNacosInstance(nacosId);
    }

    @Operation(summary = "获取Nacos实例详情")
    @GetMapping("/{nacosId}")
    public NacosResult getNacosInstance(@PathVariable String nacosId) {
        return nacosService.getNacosInstance(nacosId);
    }

    @Operation(summary = "获取Nacos中的MCP Server列表")
    @GetMapping("/{nacosId}/mcp-servers")
    public PageResult<MCPServerResult> fetchMcpServers(@PathVariable String nacosId,
                                                       Pageable pageable) {
        return nacosService.fetchMcpServers(nacosId, pageable);
    }

    @Operation(summary = "测试Nacos连接")
    @PostMapping("/{nacosId}/test-connection")
    public boolean testConnection(@PathVariable String nacosId) {
        return nacosService.testConnection(nacosId);
    }

    // ----------- MCP Server相关 -----------
    @Operation(summary = "获取指定Nacos实例中的MCP Server详情")
    @GetMapping("/{nacosId}/mcp-servers/{mcpName}")
    public McpMarketDetailParam getMcpServerDetail(@PathVariable String nacosId,
                                                   @PathVariable String mcpName,
                                                   @RequestParam(defaultValue = "public") String namespaceId,
                                                   @RequestParam(required = false) String version) {
        return nacosService.getMcpServerDetail(nacosId, mcpName, namespaceId, version);
    }
} 