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
    PageResult<ProductResult> listProducts(QueryProductParam param, Pageable pageable);

    /**
     * 更新门户
     *
     * @param productId
     * @param param
     * @return
     */
    ProductResult updateProduct(String productId, UpdateProductParam param);

    /**
     * 发布API产品
     *
     * @param productId
     * @param param
     * @return
     */
    void publishProduct(String productId, PublishProductParam param);

    /**
     * 下线产品
     *
     * @param productId
     * @param param
     * @return
     */
    void unpublishProduct(String productId, UnPublishProductParam param);

    /**
     * 删除产品
     *
     * @param productId
     */
    void deleteProduct(String productId);

    /**
     * API产品引用API或MCP Server
     *
     * @param productId
     * @param param
     */
    void addProductRef(String productId, CreateProductRefParam param);

    /**
     * 删除API产品的引用
     *
     * @param productId
     */
    void deleteProductRef(String productId);

    /**
     * 上架MCP Server为Product
     */
    ProductResult addMcpServerProduct(com.alibaba.apiopenplatform.dto.params.mcp.McpMarketCardParam param);
}
