package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.result.*;
import com.alibaba.apiopenplatform.entity.Product;
import com.alibaba.apiopenplatform.entity.ProductRef;
import com.alibaba.apiopenplatform.entity.ProductPublication;
import com.alibaba.apiopenplatform.repository.ProductRepository;
import com.alibaba.apiopenplatform.repository.ProductRefRepository;
import com.alibaba.apiopenplatform.repository.ProductPublicationRepository;
import com.alibaba.apiopenplatform.service.GatewayService;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.service.ProductService;
import com.alibaba.apiopenplatform.support.enums.ProductStatus;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import com.alibaba.apiopenplatform.support.product.RouteConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

/**
 * @author zh
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ContextHolder contextHolder;

    private final PortalService portalService;

    private final GatewayService gatewayService;

    private final ProductRepository productRepository;

    private final ProductRefRepository productRefRepository;

    private final ProductPublicationRepository publicationRepository;

    @Override
    public ProductResult createProduct(CreateProductParam param) {
        productRepository.findByNameAndAdminId(param.getName(), "admin")
                .ifPresent(APIProduct -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PRODUCT, param.getName());
                });

        String productId = IdGenerator.genApiProductId();

        Product product = param.convertTo();
        product.setProductId(productId);
        product.setAdminId("admin");
        productRepository.save(product);
        return getProduct(productId);
    }

    @Override
    public ProductResult getProduct(String productId) {
//        Product product = contextHolder.isAdministrator() ?
//                findProduct(productId) :
//                findPublishedProduct(contextHolder.getPortal(), productId);
        Product product = findProduct(productId);

        ProductResult result = new ProductResult().convertFrom(product);

        // 补充API Spec信息
        fullFillAPISpec(result);
        return result;
    }

    @Override
    public PageResult<ProductResult> listProducts(QueryProductParam param, Pageable pageable) {
        if (contextHolder.isDeveloper()) {
            param.setPortalId(contextHolder.getPortal());
        }

        Page<Product> products = productRepository.findAll(buildSpecification(param), pageable);
        return new PageResult<ProductResult>().convertFrom(
                products, product -> new ProductResult().convertFrom(product));
    }

    @Override
    public ProductResult updateProduct(String productId, UpdateProductParam param) {
        Product product = findProduct(productId);

        // 更换API产品类型
        if (param.getType() != null && product.getType() != param.getType()) {
            productRefRepository.findFirstByProductId(productId)
                    .ifPresent(productRef -> {
                        throw new BusinessException(ErrorCode.PRODUCT_API_EXISTS, product.getProductId());
                    });
        }
        param.update(product);

        // Consumer鉴权配置同步至网关
        Optional.ofNullable(param.getEnableConsumerAuth()).ifPresent(product::setEnableConsumerAuth);

        productRepository.saveAndFlush(product);
        return getProduct(product.getProductId());
    }

    @Override
    public void publishProduct(String productId, PublishProductParam param) {
        portalService.hasPortal(param.getPortalId());
        if (publicationRepository.findByPortalIdAndProduct_ProductId(param.getPortalId(), productId).isPresent()) {
            return;
        }

        Product Product = findProduct(productId);
        ProductPublication productPublication = param.convertTo();
        productPublication.setProduct(Product);

        publicationRepository.save(productPublication);
    }

    @Override
    public void unpublishProduct(String productId, UnPublishProductParam param) {
        portalService.hasPortal(param.getPortalId());

        publicationRepository.findByPortalIdAndProduct_ProductId(param.getPortalId(), productId)
                .ifPresent(publicationRepository::delete);
    }

    @Override
    public void deleteProduct(String productId) {
        Product Product = findProduct(productId);
        productRepository.delete(Product);
    }

    @Override
    public ProductResult addMcpServerProduct(com.alibaba.apiopenplatform.dto.params.mcp.McpMarketCardParam param) {
        Product product = new Product();
        product.setProductId(param.getId());
        product.setName(param.getName());
        product.setDescription(param.getDescription());
        product.setType(ProductType.MCP_SERVER);
        // icon/logo
        product.setIcon("default-icon"); // 你可以在前端或后续补充真实logo
        // 分类/能力标签
        product.setCategory(param.getCapabilities() != null && !param.getCapabilities().isEmpty() ? param.getCapabilities().get(0).toString() : "MCP");
        // 作者/团队
        product.setAdminId(""); // McpMarketCardDto没有creator字段
        // 详细文档/仓库/扩展字段
        java.util.Map<String, Object> ext = new java.util.HashMap<>();
        ext.put("version", param.getVersion());
        ext.put("versionDetail", param.getVersionDetail());
        ext.put("capabilities", param.getCapabilities());
        // ext.put("tools", param.getTools()); // McpMarketCardDto没有tools字段
        ext.put("repository", param.getRepository());
        ext.put("localServerConfig", param.getLocalServerConfig());
        ext.put("remoteServerConfig", param.getRemoteServerConfig());
        ext.put("protocol", param.getProtocol());
        ext.put("frontProtocol", param.getFrontProtocol());
        product.setDocument(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(ext).toString());
        productRepository.save(product);
        return new ProductResult().convertFrom(product);
    }

    /**
     * 查找产品，如果不存在则抛出异常
     */
    private Product findProduct(String productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PORTAL, productId));
    }

    @Override
    public void addProductRef(String productId, CreateProductRefParam param) {
        Product product = findProduct(productId);

        // 是否已存在API引用
        productRefRepository.findByProductIdAndApiId(product.getProductId(), param.getApiId())
                .ifPresent(productRef -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PRODUCT_REF, product.getProductId());
                });

        ProductRef productRef = param.convertTo();
        productRefRepository.save(productRef);
    }

    @Override
    public void deleteProductRef(String productId) {
        ProductRef productRef = productRefRepository.findFirstByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_API_NOT_FOUND, productId));

        productRefRepository.delete(productRef);
    }

    private void fullFillAPISpec(ProductResult product) {
        productRefRepository.findFirstByProductId(product.getProductId())
                .map(productRef -> {
                    try {
                        ProductType type = product.getType();
                        if (type == ProductType.REST_API) {
                            String apiSpec = gatewayService.fetchAPISpec(productRef.getGatewayId(), productRef.getApiId());
                            product.setApiSpec(apiSpec);
                        } else {
                            RouteConfig routeConfig = productRef.getRoutes().get(0);
                            String mcpSpec = gatewayService.fetchMcpSpec(productRef.getGatewayId(), productRef.getApiId(), routeConfig.getRouteId(), routeConfig.getName());
                            product.setMcpSpec(mcpSpec);
                        }
                        product.setStatus(ProductStatus.ENABLE);
                    } catch (Exception e) {
                        log.error("Failed to fetch API spec for gateway: {}, api: {}",
                                productRef.getGatewayId(), productRef.getApiId(), e);
                        product.setStatus(ProductStatus.DISABLE);
                    }
                    return product;
                })
                .orElseGet(() -> {
                    product.setStatus(ProductStatus.PENDING);
                    return product;
                });
    }

    private Product findPublishedProduct(String portalId, String productId) {
        return publicationRepository.findByPortalIdAndProduct_ProductId(portalId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PRODUCT, productId))
                .getProduct();
    }

    private Specification<Product> buildSpecification(QueryProductParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StrUtil.isNotBlank(param.getPortalId())) {
                Join<Product, ProductPublication> publication = root.join("publications");
                predicates.add(cb.equal(publication.get("portalId"), param.getPortalId()));
            }

            if (param.getType() != null) {
                predicates.add(cb.equal(root.get("type"), param.getType()));
            }

            if (StrUtil.isNotBlank(param.getCategory())) {
                predicates.add(cb.equal(root.get("category"), param.getCategory()));
            }

            if (StrUtil.isNotBlank(param.getName())) {
                String likePattern = "%" + param.getName() + "%";
                predicates.add(cb.like(root.get("name"), likePattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
