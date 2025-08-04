package com.alibaba.apiopenplatform.dto.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class MCPConfigResult {

    protected String mcpServerName;

    protected MCPServerConfig mcpServerConfig;

    protected String tools;

    protected McpMetadata meta;

    @Data
    public static class McpMetadata {

        /**
         * 来源
         * AI网关/Higress/Nacos
         */
        private String source;

        /**
         * 服务类型
         * AI网关：HTTP（HTTP转MCP）/MCP（MCP直接代理）
         * Higress：OPEN_API（OpenAPI转MCP）/DIRECT_ROUTE（直接路由）/DATABASE（数据库）
         */
        private String fromType;

    }

    @Data
    public static class MCPServerConfig {
        /**
         * for gateway
         */
        private String path;
        private List<Domain> domains;

        /**
         * for nacos
         */
        private String localConfig;
    }

    @Data
    @Builder
    public static class Domain {
        private String domain;
        private String protocol;
    }

    public static void main(String[] args) {
        System.out.println("server:\n    name: openapi-server\n    config:\n        apiKey: your-api-key-here\n    allowTools:\n        - maps-geo\ntools:\n    - name: maps-geo\n      description: Convert structured address information to latitude and longitude coordinates.\n      args:\n        - name: address\n          description: The structured address to parse\n          type: string\n          required: true\n          position: query\n        - name: city\n          description: The city to search in\n          type: string\n          position: query\n        - name: output\n          description: Output format\n          type: string\n          default: json\n          enum:\n            - json\n            - xml\n          position: query\n      requestTemplate:\n        url: https://restapi.amap.com/v3/geocode/geo\n        method: GET\n        headers:\n            - key: x-api-key\n              value: \\'{{.config.apiKey}}\\'\n      responseTemplate:\n        body: |\n            # Geocoding Information\n            {{- range $index, $geo := .geocodes }}\n            ## Location {{add $index 1}}\n            - **Country**: {{ $geo.country }}\n            - **Province**: {{ $geo.province }}\n            - **City**: {{ $geo.city }}\n            - **Coordinates**: {{ $geo.location }}\n            {{- end }}\n    - name: get_users\n      description: 获取用户列表 - 分页获取用户列表信息\n      args:\n        - name: page\n          description: 页码\n          type: integer\n          position: query\n        - name: size\n          description: 每页数量\n          type: integer\n          position: query\n      requestTemplate:\n        url: https://api.example.com/v1/users\n        method: GET\n      responseTemplate: {}\n    - name: get_users_id\n      description: 获取用户详情 - 根据用户ID获取用户详细信息\n      args:\n        - name: id\n          description: 用户ID\n          type: string\n          required: true\n          position: path\n      requestTemplate:\n        url: https://api.example.com/v1/users/{id}\n        method: GET\n      responseTemplate: {}\n    - name: post_users\n      description: 创建用户 - 创建新用户\n      args:\n        - name: email\n          description: 邮箱\n          type: string\n          position: body\n        - name: name\n          description: 用户名\n          type: string\n          position: body\n      requestTemplate:\n        url: https://api.example.com/v1/users\n        method: POST\n        headers:\n            - key: Content-Type\n              value: application/json\n      responseTemplate: {}\n");
    }
}
