package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketCardParam;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketDetailParam;
import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.service.McpMarketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * MCP Market 能力聚合接口
 * 提供 MCP Server 列表与详情查询
 * @author zxd
 */
@RestController
@RequestMapping("/mcpmarket")
@RequiredArgsConstructor
@Tag(name = "MCP server", description = "聚合Nacos MCP Server市场接口")
public class McpMarketController {
    private final McpMarketService mcpMarketService;

    @Operation(summary = "获取MCP市场列表")
    @GetMapping
    public List<McpMarketCardParam> list(@RequestParam(defaultValue = "1") int pageNo,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String mcpName,
                                         @RequestParam(defaultValue = "public") String namespaceId) {
        return mcpMarketService.list(pageNo, pageSize, mcpName, namespaceId);
    }

    @Operation(summary = "获取MCP服务详情")
    @GetMapping("/{mcpName}")
    public McpMarketDetailParam detail(@PathVariable String mcpName,
                                       @RequestParam(defaultValue = "public") String namespaceId,
                                       @RequestParam(required = false) String version) {
        return mcpMarketService.detail(mcpName, namespaceId, version);
    }
} 