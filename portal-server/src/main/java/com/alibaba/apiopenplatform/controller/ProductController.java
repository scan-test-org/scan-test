package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductResult;
import com.alibaba.apiopenplatform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Product Controller
 * @author zh
 */
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




}
