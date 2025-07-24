package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.core.annotation.AdminAuth;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.params.product.CreateProductRefParam;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductResult;
import com.alibaba.apiopenplatform.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zh
 */
@Tag(name = "API产品管理", description = "提供API产品的创建、更新、删除、查询等管理功能")
@RestController
@RequestMapping("/products")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "创建API产品")
    @PostMapping("/create")
    @AdminAuth
    public ProductResult createProduct(@RequestBody @Valid CreateProductParam param) {
        return productService.createProduct(param);
    }

    @Operation(summary = "获取API产品列表")
    @GetMapping("/list")
    public PageResult<ProductResult> listProducts(ListProductsParam param,
                                                  Pageable pageable) {
        return productService.listProducts(param, pageable);
    }

    @Operation(summary = "获取API产品详情")
    @GetMapping("/{productId}")
    public ProductResult getProduct(@PathVariable String productId) {
        return productService.getProduct(productId);
    }

    @Operation(summary = "更新API产品")
    @PostMapping("/update")
    @AdminAuth
    public ProductResult updateProduct(@RequestBody @Valid UpdateProductParam param) {
        return productService.updateProduct(param);
    }

    @Operation(summary = "发布API产品")
    @PostMapping("/publish")
    @AdminAuth
    public void publishProduct(@RequestBody @Valid PublishProductParam param) {
        productService.publishProduct(param);
    }

    @Operation(summary = "下线API产品")
    @PostMapping("/unpublish")
    @AdminAuth
    public void unpublishProduct(@RequestBody @Valid UnPublishProductParam param) {
        productService.unpublishProduct(param);
    }

    @Operation(summary = "删除API产品")
    @DeleteMapping("/{productId}")
    @AdminAuth
    public void deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
    }

    @Operation(summary = "API产品关联API或MCP Server")
    @PostMapping("/ref")
    @AdminAuth
    public void addProductRef(@RequestBody @Valid CreateProductRefParam param) {
        productService.addProductRef(param);
    }

    @Operation(summary = "删除API产品关联的API或MCP Server")
    @DeleteMapping("/ref/{productId}")
    @AdminAuth
    public void deleteProductRef(@PathVariable String productId) {
        productService.deleteProductRef(productId);
    }
}
