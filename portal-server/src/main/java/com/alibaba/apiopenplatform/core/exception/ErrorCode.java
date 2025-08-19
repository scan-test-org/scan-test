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
    AUTH_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_INVALID", "用户名或密码错误"),
    ACCOUNT_PENDING(HttpStatus.FORBIDDEN, "ACCOUNT_PENDING", "账号正在等待审批，请等待管理员审核通过后再登录"),
    ACCOUNT_NOT_FOUND(HttpStatus.UNAUTHORIZED, "ACCOUNT_NOT_FOUND", "账号不存在"),
    ACCOUNT_EXTERNAL_ONLY(HttpStatus.UNAUTHORIZED, "ACCOUNT_EXTERNAL_ONLY", "该账号不支持密码登录，请使用第三方登录"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "资源[%s:%s]不存在"),
    RESOURCE_EXIST(HttpStatus.CONFLICT, "RESOURCE_EXIST", "资源[%s:%s]已存在，请勿重复创建"),

    // 服务端错误 (500-599)
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "服务器内部错误，%s"),
    GATEWAY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GATEWAY_ERROR", "网关错误，%s"),
    PRODUCT_API_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_API_NOT_FOUND", "API产品[%s]未关联API配置"),
    PRODUCT_API_EXISTS(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_API_EXISTS", "API产品[%s]已关联API配置"),
    PRODUCT_TYPE_NOT_MATCH(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_TYPE_NOT_MATCH", "API产品[%s]类型不匹配"),


    EXTERNAL_IDENTITY_BOUND(HttpStatus.CONFLICT, "EXTERNAL_IDENTITY_BOUND", "该外部账号已被其他用户绑定"),

    // 开发者相关错误
    DEVELOPER_USERNAME_EXISTS(HttpStatus.CONFLICT, "DEVELOPER_USERNAME_EXISTS", "用户名[%s]已存在"),
    DEVELOPER_NOT_FOUND(HttpStatus.NOT_FOUND, "DEVELOPER_NOT_FOUND", "开发者[%s]不存在"),
    DEVELOPER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "DEVELOPER_UNAUTHORIZED", "开发者未授权"),
    DEVELOPER_UNBIND_FAILED(HttpStatus.BAD_REQUEST, "DEVELOPER_UNBIND_FAILED", "解绑失败，账号至少保留一种登录方式"),

    // Portal相关错误
    OIDC_CONFIG_DISABLED(HttpStatus.BAD_REQUEST, "OIDC_CONFIG_DISABLED", "OIDC配置未启用"),

    // 通用错误
    UNSUPPORTED_OPERATION(HttpStatus.BAD_REQUEST, "UNSUPPORTED_OPERATION", "不支持的操作：%s"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "无效或过期的Token"),
    PROVIDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROVIDER_NOT_FOUND", "未知的provider：%s"),

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

