package com.alibaba.apiopenplatform.core.advice;

import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应处理
 * <p>
 * 用于封装接口响应数据为统一格式：
 * {
 * "code": "Success",
 * "message": "操作成功",
 * "data": T
 * }
 * <p>
 * 以下情况不会被包装:
 * 1. 返回值已经是 {@link ResponseEntity}
 * 2. 返回值已经是 {@link Response}
 *
 * @author zh
 */
@RestControllerAdvice
@Slf4j
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 排除Swagger相关路径
        Class<?> declaringClass = returnType.getDeclaringClass();
        if (declaringClass.getName().contains("org.springdoc") ||
                declaringClass.getName().contains("springfox.documentation")) {
            return false;
        }

        return !returnType.getParameterType().equals(ResponseEntity.class)
                && !returnType.getParameterType().equals(Response.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 设置成功响应码
        response.setStatusCode(HttpStatus.OK);

        if (body instanceof String) {
            return JSONUtil.toJsonStr(Response.ok(body));
        }
        return Response.ok(body);
    }
}
