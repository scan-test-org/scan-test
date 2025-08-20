package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.support.enums.UserType;
import lombok.Builder;
import lombok.Data;

/**
 * @author zxd
 */
@Data
@Builder
public class AuthResponseResult {

    private String userId;

    private String username;

    private UserType userType;

    private String token;

    public static AuthResponseResult fromDeveloper(String developerId, String username, String token) {
        return of(developerId, username, UserType.DEVELOPER, token);
    }

    public static AuthResponseResult fromAdmin(String adminId, String username, String token) {
        return of(adminId, username, UserType.ADMIN, token);
    }

    private static AuthResponseResult of(String userId, String username, UserType userType, String token) {
        return AuthResponseResult.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .token(token)
                .build();
    }
} 