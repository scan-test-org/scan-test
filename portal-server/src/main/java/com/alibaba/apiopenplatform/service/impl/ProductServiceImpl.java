package com.alibaba.apiopenplatform.service.impl;

import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.ProductResult;
import com.alibaba.apiopenplatform.entity.Product;
import com.alibaba.apiopenplatform.entity.ProductSetting;
import com.alibaba.apiopenplatform.repository.ProductRepository;
import com.alibaba.apiopenplatform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Product Service Implementation
 * @author zh
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResult createProduct(CreateProductParam param) {
        // 检查产品名称是否已存在
        if (productRepository.findByNameAndOwnerId(param.getName(), param.getOwnerId()).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PRODUCT, param.getName());
        }

        // 生成产品ID
        String productId = IdGenerator.genApiProductId();
        
        // 转换参数为实体
        Product product = param.convertTo();
        product.setProductId(productId);

        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus("DRAFT"); // 默认状态为草稿
        }
        if (product.getEnableConsumerAuth() == null) {
            product.setEnableConsumerAuth("true"); // 默认启用消费者鉴权
        }
        if (product.getType() == null) {
            product.setType("REST_API"); // 默认类型为REST_API
        }

        // 初始化产品设置
        ProductSetting productSetting = new ProductSetting();
        productSetting.setProductId(productId);
        product.setProductSetting(productSetting);

        // 保存产品
        productRepository.save(product);

        return getProduct(productId);
    }

    @Override
    public ProductResult getProduct(String productId) {
        Product product = findProduct(productId);
        return new ProductResult().convertFrom(product);
    }

    @Override
    public PageResult<ProductResult> listProducts(String portalId, int pageNumber, int pageSize) {
        // 根据portalId查询产品列表，支持分页
        Page<Product> products = productRepository.findByOwnerId(
                portalId,
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Order.desc("gmtCreate")))
        );

        // 转换为结果对象
        Page<ProductResult> pages = products.map(product -> new ProductResult().convertFrom(product));
        return new PageResult<ProductResult>().convertFrom(pages);
    }

    @Override
    public PageResult<ProductResult> listProductsByStatus(String status, int pageNumber, int pageSize) {
        Page<Product> products = productRepository.findByStatus(
                status,
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Order.desc("gmtCreate")))
        );

        Page<ProductResult> pages = products.map(product -> new ProductResult().convertFrom(product));
        return new PageResult<ProductResult>().convertFrom(pages);
    }

    @Override
    public PageResult<ProductResult> listProductsByType(String type, int pageNumber, int pageSize) {
        Page<Product> products = productRepository.findByType(
                type,
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Order.desc("gmtCreate")))
        );

        Page<ProductResult> pages = products.map(product -> new ProductResult().convertFrom(product));
        return new PageResult<ProductResult>().convertFrom(pages);
    }

    @Override
    public PageResult<ProductResult> listProductsByCategory(String category, int pageNumber, int pageSize) {
        Page<Product> products = productRepository.findByCategory(
                category,
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Order.desc("gmtCreate")))
        );

        Page<ProductResult> pages = products.map(product -> new ProductResult().convertFrom(product));
        return new PageResult<ProductResult>().convertFrom(pages);
    }

    @Override
    public ProductResult updateProduct(UpdateProductParam param) {
        Product product = findProduct(param.getProductId());

        // 更新产品基本信息
        Optional.ofNullable(param.getName()).ifPresent(product::setName);
        Optional.ofNullable(param.getDescription()).ifPresent(product::setDescription);
        Optional.ofNullable(param.getAdminId()).ifPresent(product::setAdminId);
        Optional.ofNullable(param.getStatus()).ifPresent(product::setStatus);
        Optional.ofNullable(param.getEnableConsumerAuth()).ifPresent(product::setEnableConsumerAuth);
        Optional.ofNullable(param.getType()).ifPresent(product::setType);
        Optional.ofNullable(param.getDocument()).ifPresent(product::setDocument);
        Optional.ofNullable(param.getIcon()).ifPresent(product::setIcon);
        Optional.ofNullable(param.getCategory()).ifPresent(product::setCategory);

        productRepository.saveAndFlush(product);

        return getProduct(product.getProductId());
    }

    @Override
    public ProductResult updateProductSetting(UpdateProductSettingParam param) {
        Product product = findProduct(param.getProductId());

        ProductSetting productSetting = product.getProductSetting();
        
        // 更新产品设置
        Optional.ofNullable(param.getApiList())
                .ifPresent(productSetting::setApiList);

        Optional.ofNullable(param.getAuthType())
                .ifPresent(productSetting::setAuthType);

        Optional.ofNullable(param.getRateLimit())
                .ifPresent(productSetting::setRateLimit);

        productRepository.saveAndFlush(product);
        return getProduct(product.getProductId());
    }

    @Override
    public ProductResult publishProduct(String productId) {
        Product product = findProduct(productId);
        product.setStatus("PUBLISHED");
        productRepository.saveAndFlush(product);
        return getProduct(productId);
    }

    @Override
    public ProductResult unpublishProduct(String productId) {
        Product product = findProduct(productId);
        product.setStatus("DISABLED");
        productRepository.saveAndFlush(product);
        return getProduct(productId);
    }

    @Override
    public void deleteProduct(String productId) {
        Product product = findProduct(productId);
        productRepository.delete(product);
    }

    /**
     * 查找产品，如果不存在则抛出异常
     */
    private Product findProduct(String productId) {
        Product product = productRepository.findByProductId(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PRODUCT, productId);
        }
        return product;
    }
}
