package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.params.product.APIRefParam;
import com.alibaba.apiopenplatform.dto.result.APIRefResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductResult;
import com.alibaba.apiopenplatform.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Product Controller
 * @author zh
 */
@Tag(name = "产品管理", description = "提供产品的创建、更新、删除、查询等管理功能")
@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 创建新的Product
     * POST /products
     */
    @Operation(summary = "创建产品", description = "创建新的产品")
    @PostMapping
    public Response<ProductResult> createProduct(
            @RequestBody @Valid CreateProductParam param) {
        ProductResult result = productService.createProduct(param);
        return Response.ok(result);
    }

    /**
     * 获取Product列表
     * GET /products
     */
    @Operation(summary = "获取产品列表", description = "根据门户ID分页获取产品列表")
    @GetMapping
    public Response<PageResult<ProductResult>> listProducts(
            @Parameter(description = "门户ID", required = true)
            @RequestParam String portalId,
            @Parameter(description = "页码", required = false)
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProducts(portalId, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 根据状态获取Product列表
     * GET /products/status/{status}
     */
    @Operation(summary = "根据状态获取产品列表", description = "根据产品状态分页获取产品列表")
    @GetMapping("/status/{status}")
    public Response<PageResult<ProductResult>> listProductsByStatus(
            @Parameter(description = "产品状态", required = true)
            @PathVariable String status,
            @Parameter(description = "页码", required = false)
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProductsByStatus(status, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 根据类型获取Product列表
     * GET /products/type/{type}
     */
    @Operation(summary = "根据类型获取产品列表", description = "根据产品类型分页获取产品列表")
    @GetMapping("/type/{type}")
    public Response<PageResult<ProductResult>> listProductsByType(
            @Parameter(description = "产品类型", required = true)
            @PathVariable String type,
            @Parameter(description = "页码", required = false)
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProductsByType(type, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 根据分类获取Product列表
     * GET /products/category/{category}
     */
    @Operation(summary = "根据分类获取产品列表", description = "根据产品分类分页获取产品列表")
    @GetMapping("/category/{category}")
    public Response<PageResult<ProductResult>> listProductsByCategory(
            @Parameter(description = "产品分类", required = true)
            @PathVariable String category,
            @Parameter(description = "页码", required = false)
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ProductResult> result = productService.listProductsByCategory(category, pageNumber, pageSize);
        return Response.ok(result);
    }

    /**
     * 获取特定Product详情
     * GET /products/{id}
     */
    @Operation(summary = "获取产品详情", description = "根据产品ID获取产品详细信息")
    @GetMapping("/{id}")
    public Response<ProductResult> getProduct(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id) {
        ProductResult result = productService.getProduct(id);
        return Response.ok(result);
    }

    /**
     * 更新Product信息
     * PUT /products/{id}
     */
    @Operation(summary = "更新产品", description = "根据产品ID更新产品信息")
    @PutMapping("/{id}")
    public Response<ProductResult> updateProduct(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id,
            @RequestBody @Valid UpdateProductParam param) {
        param.setProductId(id);
        ProductResult result = productService.updateProduct(param);
        return Response.ok(result);
    }

    /**
     * 发布Product
     * PUT /products/{id}/publish
     */
    @Operation(summary = "发布产品", description = "根据产品ID发布产品")
    @PutMapping("/{id}/publish")
    public Response<ProductResult> publishProduct(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id) {
        ProductResult result = productService.publishProduct(id);
        return Response.ok(result);
    }

    /**
     * 下线Product
     * PUT /products/{id}/unpublish
     */
    @Operation(summary = "下线产品", description = "根据产品ID下线产品")
    @PutMapping("/{id}/unpublish")
    public Response<ProductResult> unpublishProduct(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id) {
        ProductResult result = productService.unpublishProduct(id);
        return Response.ok(result);
    }

    /**
     * 删除Product
     * DELETE /products/{id}
     */
    @Operation(summary = "删除产品", description = "根据产品ID删除产品")
    @DeleteMapping("/{id}")
    public Response<Void> deleteProduct(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id) {
        productService.deleteProduct(id);
        return Response.ok(null);
    }

    /**
     * 为产品添加API引用
     * POST /products/{id}/api-refs
     */
    @Operation(summary = "为产品添加API引用", description = "为指定产品添加API引用")
    @PostMapping("/{id}/api-refs")
    public Response<APIRefResult> addAPIRef(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id,
            @RequestBody @Valid APIRefParam param) {
        param.setProductId(id);
        APIRefResult result = productService.addAPIRef(param);
        return Response.ok(result);
    }

    /**
     * 获取产品的API引用列表
     * GET /products/{id}/api-refs
     */
    @Operation(summary = "获取产品的API引用列表", description = "根据产品ID获取该产品的所有API引用")
    @GetMapping("/{id}/api-refs")
    public Response<List<APIRefResult>> getAPIRefs(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id) {
        List<APIRefResult> result = productService.getAPIRefsByProductId(id);
        return Response.ok(result);
    }

    /**
     * 删除产品的API引用
     * DELETE /products/{id}/api-refs/{apiId}
     */
    @Operation(summary = "删除产品的API引用", description = "删除指定产品的特定API引用")
    @DeleteMapping("/{id}/api-refs/{apiId}")
    public Response<Void> deleteAPIRef(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id,
            @Parameter(description = "API ID", required = true)
            @PathVariable String apiId) {
        productService.deleteAPIRef(id, apiId);
        return Response.ok(null);
    }

    /**
     * 批量添加API引用
     * POST /products/{id}/api-refs/batch
     */
    @Operation(summary = "批量添加API引用", description = "为指定产品批量添加多个API引用")
    @PostMapping("/{id}/api-refs/batch")
    public Response<List<APIRefResult>> batchAddAPIRefs(
            @Parameter(description = "产品ID", required = true)
            @PathVariable String id,
            @RequestBody @Valid List<APIRefParam> apiRefParams) {
        List<APIRefResult> result = productService.batchAddAPIRefs(id, apiRefParams);
        return Response.ok(result);
    }
}
