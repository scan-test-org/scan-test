package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.product.CreateProductParam;
import com.alibaba.apiopenplatform.dto.params.product.UpdateProductParam;
import com.alibaba.apiopenplatform.dto.params.product.UpdateProductSettingParam;
import com.alibaba.apiopenplatform.dto.params.product.APIRefParam;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductResult;
import com.alibaba.apiopenplatform.dto.result.APIRefResult;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author zh
 */
public interface ProductService {

    /**
     * 创建门户
     *
     * @param param
     * @return
     */
    ProductResult createProduct(CreateProductParam param);

    /**
     * 查询门户
     *
     * @param portalId
     * @return
     */
    ProductResult getProduct(String productId);

    /**
     * 查询产品列表
     *
     * @param portalId
     * @param pageNumber
     * @param pageSize
     * @return
     */
    PageResult<ProductResult> listProducts(String portalId, int pageNumber, int pageSize);

    /**
     * 根据状态查询产品列表
     *
     * @param status
     * @param pageNumber
     * @param pageSize
     * @return
     */
    PageResult<ProductResult> listProductsByStatus(String status, int pageNumber, int pageSize);

    /**
     * 根据类型查询产品列表
     *
     * @param type
     * @param pageNumber
     * @param pageSize
     * @return
     */
    PageResult<ProductResult> listProductsByType(String type, int pageNumber, int pageSize);

    /**
     * 根据分类查询产品列表
     *
     * @param category
     * @param pageNumber
     * @param pageSize
     * @return
     */
    PageResult<ProductResult> listProductsByCategory(String category, int pageNumber, int pageSize);

    /**
     * 更新门户
     *
     * @param param
     * @return
     */
    ProductResult updateProduct(UpdateProductParam param);

    /**
     * 更新产品设置
     *
     * @param param
     * @return
     */
    ProductResult updateProductSetting(UpdateProductSettingParam param);

    /**
     * 发布产品
     *
     * @param productId
     * @return
     */
    ProductResult publishProduct(String productId);

    /**
     * 下线产品
     *
     * @param productId
     * @return
     */
    ProductResult unpublishProduct(String productId);

    /**
     * 删除产品
     *
     * @param productId
     */
    void deleteProduct(String productId);

    /**
     * 为产品添加API引用
     *
     * @param param
     * @return
     */
    APIRefResult addAPIRef(APIRefParam param);

    /**
     * 获取产品的API引用列表
     *
     * @param productId
     * @return
     */
    List<APIRefResult> getAPIRefsByProductId(String productId);

    /**
     * 删除产品的API引用
     *
     * @param productId
     * @param apiId
     */
    void deleteAPIRef(String productId, String apiId);

    /**
     * 批量添加API引用
     *
     * @param productId
     * @param apiRefParams
     * @return
     */
    List<APIRefResult> batchAddAPIRefs(String productId, List<APIRefParam> apiRefParams);
}
