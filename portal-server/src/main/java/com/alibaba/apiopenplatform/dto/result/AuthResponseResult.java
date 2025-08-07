package com.alibaba.apiopenplatform.dto.result;

import lombok.Data;

/**
 * 认证响应DTO，包含token和基础信息
 *
 * @author zxd
 */
@Data
public class AuthResponseResult {
    private String token;
    private String userId;
    private String username;
    private String status;
    private String userType;
    
    /**
     * 从Developer构建认证响应
     */
    public static AuthResponseResult fromDeveloper(String userId, String username, String token) {
        AuthResponseResult result = new AuthResponseResult();
        result.setToken(token);
        result.setUserId(userId);
        result.setUsername(username);
        result.setUserType("developer");
        return result;
    }
    
    /**
     * 从Administrator构建认证响应
     */
    public static AuthResponseResult fromAdmin(String userId, String username, String token) {
        AuthResponseResult result = new AuthResponseResult();
        result.setToken(token);
        result.setUserId(userId);
        result.setUsername(username);
        result.setUserType("admin");
        return result;
    }
} 