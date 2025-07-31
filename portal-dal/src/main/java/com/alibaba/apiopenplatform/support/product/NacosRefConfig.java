package com.alibaba.apiopenplatform.support.product;

import lombok.Data;

/**
 * Nacos MCP Server 配置
 * @author zxd
 */
@Data
public class NacosRefConfig {

    private String mcpServerName;

    private String namespaceId;

    private String version;
} 