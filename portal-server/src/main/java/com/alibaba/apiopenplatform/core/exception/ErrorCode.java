package com.alibaba.apiopenplatform.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author zh
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 客户端错误 (400-499)
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "请求参数[%s]的值无效或格式错误"),

    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "请先登录后再进行操作"),
    AUTH_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_INVALID", "用户认证失败，请检查账号密码"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "资源[%s:%s]不存在"),
    RESOURCE_EXIST(HttpStatus.CONFLICT, "RESOURCE_EXIST", "资源[%s:%s]已存在，请勿重复创建"),

    // 服务端错误 (500-599)
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "服务器内部错误，%s"),
    GATEWAY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GATEWAY_ERROR", "网关错误，%s"),

    ADMIN_ALREADY_INITIALIZED(HttpStatus.CONFLICT, "ADMIN_ALREADY_INITIALIZED", "该portal已初始化管理员，禁止重复初始化"),
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_NOT_FOUND", "管理员[%s]不存在"),
    ADMIN_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "ADMIN_PASSWORD_INCORRECT", "原密码错误"),
    ADMIN_USERNAME_EXISTS(HttpStatus.CONFLICT, "ADMIN_USERNAME_EXISTS", "用户名[%s]已存在"),
    ADMIN_INIT_REQUIRED(HttpStatus.FORBIDDEN, "ADMIN_INIT_REQUIRED", "请先初始化管理员账户"),

    EXTERNAL_IDENTITY_BOUND(HttpStatus.CONFLICT, "EXTERNAL_IDENTITY_BOUND", "该外部账号已被其他用户绑定"),

    ;

    private final HttpStatus status;
    private final String code;
    private final String messagePattern;

    public String getMessage(Object... args) {
        try {
            return String.format(messagePattern, args);
        } catch (Exception e) {
            // 参数不匹配时，直接返回原始 messagePattern，避免抛出异常
            return messagePattern;
        }
    }
}

