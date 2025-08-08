package com.alibaba.apiopenplatform.support.consumer;

import lombok.Data;

@Data
public class ConsumerAuthorizationRule {

    private Long id;
    private String consumerAuthorizationRuleId;
    private String consumerId;
    private String expireMode;
    private Long expireTimestamp;
    private String gatewayId;
    private String resourceId;

    /**
     * 资源类型枚举值说明：
     * HttpApiRoute     - HTTP API路由
     * RestApi          - REST API
     * RestApiOperation - REST API操作
     * HttpApi          - HTTP API（暂不支持授权）
     * AiApi            - AI API
     * LLM              - 大语言模型
     * Agent            - 智能体
     * MCP              - MCP服务
     * MCPTool          - MCP工具
     */
    private String resourceType;
}
