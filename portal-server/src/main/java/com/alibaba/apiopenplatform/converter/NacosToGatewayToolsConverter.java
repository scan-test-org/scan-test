package com.alibaba.apiopenplatform.converter;

import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class NacosToGatewayToolsConverter {
    
    @Data
    public static class Server {
        private String name;
        private Map<String, Object> config = new HashMap<>();
    }
    
    @Data
    public static class Tool {
        private String name;
        private String description;
        private List<Arg> args = new ArrayList<>();
        private RequestTemplate requestTemplate;
        private ResponseTemplate responseTemplate;
    }
    
    @Data
    public static class Arg {
        private String name;
        private String description;
        private String type;
        private boolean required;
        private String position;
        private String defaultValue;
        private List<String> enumValues;
    }
    
    @Data
    public static class RequestTemplate {
        private String url;
        private String method;
        private List<Header> headers = new ArrayList<>();
    }
    
    @Data
    public static class ResponseTemplate {
        private String body;
    }
    
    @Data
    public static class Header {
        private String key;
        private String value;
    }
    
    private Server server = new Server();
    private List<Tool> tools = new ArrayList<>();
    private List<String> allowTools = new ArrayList<>();
    
    public void convertFromNacos(McpServerDetailInfo nacosDetail) {
        // 设置server信息
        server.setName(nacosDetail.getName());
        server.getConfig().put("apiKey", "your-api-key-here");
        allowTools.add(nacosDetail.getName());
        
        // 转换tools
        if (nacosDetail.getToolSpec() != null) {
            convertTools(nacosDetail.getToolSpec());
        }
    }
    
    private void convertTools(Object toolSpec) {
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            String toolSpecJson = jsonMapper.writeValueAsString(toolSpec);
            JsonNode toolSpecNode = jsonMapper.readTree(toolSpecJson);
            
            if (toolSpecNode.isArray()) {
                // 如果是数组格式
                for (JsonNode toolNode : toolSpecNode) {
                    Tool tool = convertToolNode(toolNode);
                    if (tool != null) {
                        tools.add(tool);
                    }
                }
            } else if (toolSpecNode.has("tools") && toolSpecNode.get("tools").isArray()) {
                // 如果是包含tools字段的对象
                JsonNode toolsNode = toolSpecNode.get("tools");
                for (JsonNode toolNode : toolsNode) {
                    Tool tool = convertToolNode(toolNode);
                    if (tool != null) {
                        tools.add(tool);
                    }
                }
            }
        } catch (Exception e) {
            // 转换失败时，tools保持空列表
        }
    }
    
    private Tool convertToolNode(JsonNode toolNode) {
        Tool result = new Tool();
        result.setName(getStringValue(toolNode, "name"));
        result.setDescription(getStringValue(toolNode, "description"));
        
        if (result.getName() == null) {
            return null; // 跳过没有name的工具
        }
        
        // 转换args
        List<Arg> args = convertArgs(toolNode);
        result.setArgs(args);
        
        // 设置默认的requestTemplate和responseTemplate
        result.setRequestTemplate(buildDefaultRequestTemplate(result.getName()));
        result.setResponseTemplate(buildDefaultResponseTemplate());
        
        return result;
    }
    
    private List<Arg> convertArgs(JsonNode toolNode) {
        List<Arg> args = new ArrayList<>();
        
        try {
            if (toolNode.has("inputSchema") && toolNode.get("inputSchema").has("properties")) {
                // 处理inputSchema格式
                JsonNode properties = toolNode.get("inputSchema").get("properties");
                properties.fields().forEachRemaining(entry -> {
                    String argName = entry.getKey();
                    JsonNode argNode = entry.getValue();
                    
                    Arg arg = new Arg();
                    arg.setName(argName);
                    arg.setDescription(getStringValue(argNode, "description"));
                    arg.setType(getStringValue(argNode, "type"));
                    arg.setRequired(true); // 默认设为required
                    arg.setPosition("query"); // 默认position
                    
                    args.add(arg);
                });
            } else if (toolNode.has("args") && toolNode.get("args").isArray()) {
                // 处理args格式
                JsonNode argsNode = toolNode.get("args");
                for (JsonNode argNode : argsNode) {
                    Arg arg = new Arg();
                    arg.setName(getStringValue(argNode, "name"));
                    arg.setDescription(getStringValue(argNode, "description"));
                    arg.setType(getStringValue(argNode, "type"));
                    arg.setRequired(getBooleanValue(argNode, "required", false));
                    arg.setPosition(getStringValue(argNode, "position"));
                    arg.setDefaultValue(getStringValue(argNode, "default"));
                    
                    args.add(arg);
                }
            }
        } catch (Exception e) {
            // 转换失败时，args保持空列表
        }
        
        return args;
    }
    
    private RequestTemplate buildDefaultRequestTemplate(String toolName) {
        RequestTemplate template = new RequestTemplate();
        template.setUrl("https://api.example.com/v1/" + toolName);
        template.setMethod("GET");
        
        Header header = new Header();
        header.setKey("Content-Type");
        header.setValue("application/json");
        template.getHeaders().add(header);
        
        return template;
    }
    
    private ResponseTemplate buildDefaultResponseTemplate() {
        ResponseTemplate template = new ResponseTemplate();
        template.setBody("");
        return template;
    }
    
    private String getStringValue(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.get(fieldName).isNull() ? 
            node.get(fieldName).asText() : null;
    }
    
    private boolean getBooleanValue(JsonNode node, String fieldName, boolean defaultValue) {
        return node.has(fieldName) && !node.get(fieldName).isNull() ? 
            node.get(fieldName).asBoolean() : defaultValue;
    }
    
    public String toYaml() {
        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            return yamlMapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to YAML", e);
        }
    }
}
