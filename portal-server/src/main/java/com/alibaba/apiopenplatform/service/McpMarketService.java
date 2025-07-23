package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.mcp.McpMarketCardDto;
import com.alibaba.apiopenplatform.dto.mcp.McpMarketDetailDto;
import java.util.List;

/**
 * MCP Market 聚合服务接口
 * @author zxd
 */
public interface McpMarketService {
    /**
     * 获取MCP能力市场卡片列表，支持namespaceId
     */
    List<McpMarketCardDto> list(int pageNo, int pageSize, String mcpName, String namespaceId);

    /**
     * 获取MCP能力服务详情，支持namespaceId和version
     */
    McpMarketDetailDto detail(String mcpName, String namespaceId, String version);
} 