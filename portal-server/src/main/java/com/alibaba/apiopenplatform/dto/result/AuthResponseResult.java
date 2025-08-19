package com.alibaba.apiopenplatform.dto.result;

import lombok.Data;

/**
 * @author zxd
 */
@Data
public class AuthResponseResult {

    private String userId;

    private String username;

    private String userType;

    private String status;

    private String token;

    /**
     * 从Developer构建认证响应
     */
    public static AuthResponseResult fromDeveloper(String userId, String username, String token) {
        AuthResponseResult result = new AuthResponseResult();
        result.setUserId(userId);
        result.setUsername(username);
        result.setUserType("developer");
        result.setToken(token);
        return result;
    }

    /**
     * 从Administrator构建认证响应
     */
    public static AuthResponseResult fromAdmin(String userId, String username, String token) {
        AuthResponseResult result = new AuthResponseResult();
        result.setUserId(userId);
        result.setUsername(username);
        result.setUserType("admin");
        result.setToken(token);
        return result;
    }
} 