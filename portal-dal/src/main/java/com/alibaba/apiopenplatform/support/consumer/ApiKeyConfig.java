package com.alibaba.apiopenplatform.support.consumer;

import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class ApiKeyConfig {

    private List<String> apiKeys;

    /**
     * apikey的位置
     */
    private String source;

    /**
     * apikey参数名称
     */
    private String key;
}