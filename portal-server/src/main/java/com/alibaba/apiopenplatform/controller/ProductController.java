package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductResult;
import com.alibaba.apiopenplatform.dto.result.ProductVersionResult;
import com.alibaba.apiopenplatform.dto.result.ProductConfigResult;
import com.alibaba.apiopenplatform.service.ProductService;
import com.alibaba.apiopenplatform.service.ProductVersionService;
import com.alibaba.apiopenplatform.service.ProductConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Product Controller
 * @author zh
 */
@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductVersionService productVersionService;
    private final ProductConfigService productConfigService;

    public ProductController(ProductService productService, 
                           ProductVersionService productVersionService,
                           ProductConfigService productConfigService) {
        this.productService = productService;
        this.productVersionService = productVersionService;
        this.productConfigService = productConfigService;
    }

    /**
     * 创建新的Product
     * POST /products
     */
    @PostMapping
    public Response<ProductResult> createProduct(@Valid @RequestBody CreateProductParam param) {
        ProductResult result = productService.createProduct(param);
        return Response.ok(result);
    }

    /**
     * 获取Product列表
     * GET /products
     */
    @GetMapping
    public Response<PageResult<ProductResult>> listProducts(
            @RequestParam String portalId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProducts(portalId, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 根据状态获取Product列表
     * GET /products/status/{status}
     */
    @GetMapping("/status/{status}")
    public Response<PageResult<ProductResult>> listProductsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProductsByStatus(status, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 根据类型获取Product列表
     * GET /products/type/{type}
     */
    @GetMapping("/type/{type}")
    public Response<PageResult<ProductResult>> listProductsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProductsByType(type, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 根据分类获取Product列表
     * GET /products/category/{category}
     */
    @GetMapping("/category/{category}")
    public Response<PageResult<ProductResult>> listProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProductsByCategory(category, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 获取特定Product详情
     * GET /products/{id}
     */
    @GetMapping("/{id}")
    public Response<ProductResult> getProduct(@PathVariable String id) {
        ProductResult result = productService.getProduct(id);
        return Response.ok(result);
    }

    /**
     * 更新Product信息
     * PUT /products/{id}
     */
    @PutMapping("/{id}")
    public Response<ProductResult> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductParam param) {
        param.setProductId(id);
        ProductResult result = productService.updateProduct(param);
        return Response.ok(result);
    }

    /**
     * 发布Product
     * PUT /products/{id}/publish
     */
    @PutMapping("/{id}/publish")
    public Response<ProductResult> publishProduct(@PathVariable String id) {
        ProductResult result = productService.publishProduct(id);
        return Response.ok(result);
    }

    /**
     * 下线Product
     * PUT /products/{id}/unpublish
     */
    @PutMapping("/{id}/unpublish")
    public Response<ProductResult> unpublishProduct(@PathVariable String id) {
        ProductResult result = productService.unpublishProduct(id);
        return Response.ok(result);
    }

    /**
     * 删除Product
     * DELETE /products/{id}
     */
    @DeleteMapping("/{id}")
    public Response<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return Response.ok(null);
    }

    /**
     * 创建新的Product版本
     * POST /products/{id}/versions
     */
    @PostMapping("/{id}/versions")
    public Response<ProductVersionResult> createProductVersion(
            @PathVariable String id,
            @Valid @RequestBody CreateProductVersionParam param) {
        param.setProductId(id);
        ProductVersionResult result = productVersionService.createVersion(param);
        return Response.ok(result);
    }

    /**
     * 获取Product的版本列表
     * GET /products/{id}/versions
     */
    @GetMapping("/{id}/versions")
    public Response<List<ProductVersionResult>> listProductVersions(@PathVariable String id) {
        List<ProductVersionResult> result = productVersionService.listVersions(id);
        return Response.ok(result);
    }

    /**
     * 更新Product版本
     * PUT /products/{id}/versions/{version_id}
     */
    @PutMapping("/{id}/versions/{versionId}")
    public Response<ProductVersionResult> updateProductVersion(
            @PathVariable String id,
            @PathVariable String versionId,
            @Valid @RequestBody UpdateProductVersionParam param) {
        param.setProductId(id);
        param.setVersionId(versionId);
        ProductVersionResult result = productVersionService.updateVersion(param);
        return Response.ok(result);
    }

    /**
     * 添加或更新Product配置
     * POST /products/{id}/configs
     */
    @PostMapping("/{id}/configs")
    public Response<ProductConfigResult> addOrUpdateProductConfig(
            @PathVariable String id,
            @Valid @RequestBody ProductConfigParam param) {
        param.setProductId(id);
        ProductConfigResult result = productConfigService.addOrUpdateConfig(param);
        return Response.ok(result);
    }

    /**
     * 获取Product的配置列表
     * GET /products/{id}/configs
     */
    @GetMapping("/{id}/configs")
    public Response<List<ProductConfigResult>> listProductConfigs(@PathVariable String id) {
        List<ProductConfigResult> result = productConfigService.listConfigs(id);
        return Response.ok(result);
    }

    /**
     * 删除Product配置
     * DELETE /products/{id}/configs/{config_key}
     */
    @DeleteMapping("/{id}/configs/{configKey}")
    public Response<Void> deleteProductConfig(
            @PathVariable String id,
            @PathVariable String configKey) {
        productConfigService.deleteConfig(id, configKey);
        return Response.ok(null);
    }
}
