package com.alibaba.apiopenplatform.support.product;

import lombok.Data;

/**
 * Nacos MCP Server 配置
 * @author zxd
 */
@Data
public class NacosRefConfig {
    
    /**
     * MCP Server 名称
     */
    private String mcpServerName;
    
    /**
     * 命名空间ID
     */
    private String namespaceId;
    
    /**
     * 版本号
     */
    private String version;
} 