package com.alibaba.apiopenplatform.support.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zh
 */
@Getter
@RequiredArgsConstructor
public enum APIGAPIType {

    REST("Rest"),

    HTTP("Http"),

    MCP("MCP"),

    ;

    private final String type;
}
