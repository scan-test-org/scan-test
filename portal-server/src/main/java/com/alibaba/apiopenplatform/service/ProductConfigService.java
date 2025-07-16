package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.product.ProductConfigParam;
import com.alibaba.apiopenplatform.dto.result.ProductConfigResult;

import java.util.List;

/**
 * Product Config Service
 * @author zh
 */
public interface ProductConfigService {

    /**
     * 添加或更新Product配置
     */
    ProductConfigResult addOrUpdateConfig(ProductConfigParam param);

    /**
     * 获取Product配置列表
     */
    List<ProductConfigResult> listConfigs(String productId);

    /**
     * 删除Product配置
     */
    void deleteConfig(String productId, String configKey);
} 