package com.alibaba.apiopenplatform.support.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zh
 */
@RequiredArgsConstructor
@Getter
public enum GatewayType {

    /**
     * 云原生API网关
     */
    APIG_API("API"),

    /**
     * AI网关
     */
    APIG_AI("AI"),

    /**
     * Higress
     */
    HIGRESS("Higress"),

    ;

    private final String type;

    public boolean isHigress() {
        return this == HIGRESS;
    }

    public boolean isAPIG() {
        return this == APIG_API || this == APIG_AI;
    }
}
