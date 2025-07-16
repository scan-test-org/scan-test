package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.product.CreateProductVersionParam;
import com.alibaba.apiopenplatform.dto.params.product.UpdateProductVersionParam;
import com.alibaba.apiopenplatform.dto.result.ProductVersionResult;

import java.util.List;

/**
 * Product Version Service
 * @author zh
 */
public interface ProductVersionService {

    /**
     * 创建Product版本
     */
    ProductVersionResult createVersion(CreateProductVersionParam param);

    /**
     * 获取Product版本列表
     */
    List<ProductVersionResult> listVersions(String productId);

    /**
     * 更新Product版本
     */
    ProductVersionResult updateVersion(UpdateProductVersionParam param);

    /**
     * 删除Product版本
     */
    void deleteVersion(String productId, String versionId);
} 