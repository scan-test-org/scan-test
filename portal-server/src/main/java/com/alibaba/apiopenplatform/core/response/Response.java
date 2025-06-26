package com.alibaba.apiopenplatform.core.response;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zh
 */
@Data
@Accessors(chain = true)
public class Response<T> {
    private String code;
    private String message;
    private T data;

    public static <T> Response<T> ok(T data) {
        return new Response<T>()
                .setCode("Success")
                .setData(data);
    }

    public static <T> Response<T> fail(String code, String message) {
        return new Response<T>()
                .setCode(code)
                .setMessage(message);
    }
}

