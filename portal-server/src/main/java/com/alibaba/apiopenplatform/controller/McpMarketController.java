package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.mcp.McpMarketCardDto;
import com.alibaba.apiopenplatform.dto.mcp.McpMarketDetailDto;
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
@RequestMapping("/api/mcpmarket")
@RequiredArgsConstructor
@Tag(name = "MCP能力市场", description = "聚合Nacos MCP Server能力市场接口")
public class McpMarketController {
    private final McpMarketService mcpMarketService;

    @Operation(summary = "获取MCP能力市场列表")
    @GetMapping("/list")
    public Response<List<McpMarketCardDto>> list(@RequestParam(defaultValue = "1") int pageNo,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String mcpName,
                                                 @RequestParam(defaultValue = "public") String namespaceId) {
        return Response.ok(mcpMarketService.list(pageNo, pageSize, mcpName, namespaceId));
    }

    @Operation(summary = "获取MCP能力服务详情")
    @GetMapping("/detail/{mcpName}")
    public Response<McpMarketDetailDto> detail(@PathVariable String mcpName,
                                               @RequestParam(defaultValue = "public") String namespaceId,
                                               @RequestParam(required = false) String version) {
        return Response.ok(mcpMarketService.detail(mcpName, namespaceId, version));
    }
} 