package com.alibaba.apiopenplatform.support.enums;

/**
 * @author zh
 */
public enum ProductStatus {

    /**
     * 未配置API
     */
    PENDING,

    /**
     * API可用
     */
    ENABLE,

    /**
     * API不可用
     */
    DISABLE,

    ;

    public boolean isReady() {
        return this != PENDING;
    }
}
