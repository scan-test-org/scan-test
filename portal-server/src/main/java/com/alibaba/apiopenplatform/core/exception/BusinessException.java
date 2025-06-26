package com.alibaba.apiopenplatform.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author zh
 */
@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String message;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage(args));
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage(args);
    }
}