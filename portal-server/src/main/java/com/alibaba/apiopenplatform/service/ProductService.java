package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.core.event.PortalDeletingEvent;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductPublicationResult;
import com.alibaba.apiopenplatform.dto.result.ProductRefResult;
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
     * @param portalId
     * @return
     */
    void publishProduct(String productId, String portalId);

    /**
     * 获取API产品的发布信息
     *
     * @param productId
     * @param pageable
     * @return
     */
    PageResult<ProductPublicationResult> getPublications(String productId, Pageable pageable);

    /**
     * 下线产品
     *
     * @param productId
     * @param portalId
     * @return
     */
    void unpublishProduct(String productId, String portalId);

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
    void addProductRef(String productId, CreateProductRefParam param) throws Exception;

    /**
     * 查询API产品引用的资源
     *
     * @param productId
     * @return
     */
    ProductRefResult getProductRef(String productId);

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


    /**
     * 清理门户资源
     *
     * @param event
     */
    void handlePortalDeletion(PortalDeletingEvent event);
}
