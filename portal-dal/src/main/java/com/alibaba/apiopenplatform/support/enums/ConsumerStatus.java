package com.alibaba.apiopenplatform.support.enums;

/**
 * @author zh
 */
public enum ConsumerStatus {

    /**
     * 待审核
     */
    PENDING,

    /**
     * 已审核
     */
    APPROVED,

    /**
     * 不可用，对应的网关资源已删除
     */
    DISABLED,

    ;
}
