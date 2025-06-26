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
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "服务器内部错误，请稍后重试"),


    ;

    private final HttpStatus status;
    private final String code;
    private final String messagePattern;

    public String getMessage(Object... args) {
        return String.format(messagePattern, args);
    }
}

