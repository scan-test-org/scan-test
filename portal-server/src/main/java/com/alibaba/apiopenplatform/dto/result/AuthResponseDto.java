package com.alibaba.apiopenplatform.dto.result;

import lombok.Data;

/**
 * 认证响应DTO，包含token和基础信息
 *
 * @author zxd
 */
@Data
public class AuthResponseDto {
    private String token;
    private String userId;
    private String username;
    private String status;
    private String userType;
} 