package com.alibaba.apiopenplatform.dto.result;

import lombok.Data;

/**
 * @author zh
 */
@Data
public class APIConfigResult {

    private String spec;

    private APIMetadata meta;

    @Data
    public static class APIMetadata {

        /**
         * 来源
         * API网关/Higress
         */
        private String source;

        /**
         * 类型
         * API网关：HTTP/REST
         * Higress：Route
         */
        private String type;
    }
}
