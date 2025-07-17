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
     * 获取MCP能力市场卡片列表
     */
    List<McpMarketCardDto> list(int pageNo, int pageSize, String keyword);

    /**
     * 获取MCP能力服务详情
     */
    McpMarketDetailDto detail(String mcpId);
} 