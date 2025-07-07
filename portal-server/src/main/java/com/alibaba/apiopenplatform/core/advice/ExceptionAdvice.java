package com.alibaba.apiopenplatform.core.advice;

import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理
 * <p>
 * 处理三类异常:
 * 1. 业务异常 {@link BusinessException}: 业务异常
 * 2. 参数校验异常 {@link MethodArgumentNotValidException}: 请求参数校验不通过
 * 3. 系统异常 {@link Exception}: 非预期的系统异常
 * <p>
 * 所有异常都会被转换为统一的响应格式:
 * {
 * "code": "错误码",
 * "message": "错误信息",
 * "data": null
 * }
 *
 * @author zh
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(Response.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleParamVerifyException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER.getStatus())
                .body(Response.fail(ErrorCode.INVALID_PARAMETER.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleSystemException(Exception e) {
        String message = String.format("%s: %s", ErrorCode.INTERNAL_ERROR.getMessage(), e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(Response.fail(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        message
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<Void>> handleRuntimeException(RuntimeException e) {
        log.warn("[RuntimeException] {}", e.getMessage(), e);
        return ResponseEntity
                .status(400)
                .body(Response.fail("FAIL", e.getMessage()));
    }
}
