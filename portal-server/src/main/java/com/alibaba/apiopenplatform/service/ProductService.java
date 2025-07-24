package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductResult;
import org.springframework.data.domain.Pageable;

/**
 * @author zh
 */
public interface ProductService {

    /**
     * 创建API产品
     *
     * @param param
     * @return
     */
    ProductResult createProduct(CreateProductParam param);

    /**
     * 查询API产品
     *
     * @param productId
     * @return
     */
    ProductResult getProduct(String productId);

    /**
     * 查询API产品列表
     *
     * @param param
     * @param pageable
     * @return
     */
    PageResult<ProductResult> listProducts(ListProductsParam param, Pageable pageable);

    /**
     * 更新门户
     *
     * @param param
     * @return
     */
    ProductResult updateProduct(UpdateProductParam param);

    /**
     * 发布API产品
     *
     * @param param
     * @return
     */
    void publishProduct(PublishProductParam param);

    /**
     * 下线产品
     *
     * @param param
     * @return
     */
    void unpublishProduct(UnPublishProductParam param);

    /**
     * 删除产品
     *
     * @param productId
     */
    void deleteProduct(String productId);

    /**
     * API产品引用API或MCP Server
     *
     * @param param
     */
    void addProductRef(CreateProductRefParam param);

    /**
     * 删除API产品的引用
     *
     * @param productId
     */
    void deleteProductRef(String productId);
}
