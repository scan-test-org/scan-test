package com.alibaba.apiopenplatform.support.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zh
 */
@RequiredArgsConstructor
@Getter
public enum HigressAPIType {

    ROUTE("Route"),

    MCP("MCP"),

    ;

    private final String type;
}
