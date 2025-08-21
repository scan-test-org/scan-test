/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
