package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.event.PortalDeletingEvent;
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
import com.alibaba.apiopenplatform.service.NacosService;
import com.alibaba.apiopenplatform.support.enums.ProductStatus;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ContextHolder contextHolder;

    private final PortalService portalService;

    private final GatewayService gatewayService;

    private final ProductRepository productRepository;

    private final ProductRefRepository productRefRepository;

    private final ProductPublicationRepository publicationRepository;

    private final NacosService nacosService;

    @Override
    public ProductResult createProduct(CreateProductParam param) {
        productRepository.findByNameAndAdminId(param.getName(), contextHolder.getUser())
                .ifPresent(APIProduct -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PRODUCT, param.getName());
                });

        String productId = IdGenerator.genApiProductId();

        Product product = param.convertTo();
        product.setProductId(productId);
        product.setAdminId(contextHolder.getUser());
        productRepository.save(product);

        return getProduct(productId);
    }

    @Override
    public ProductResult getProduct(String productId) {
        Product product = contextHolder.isAdministrator() ?
                findProduct(productId) :
                findPublishedProduct(contextHolder.getPortal(), productId);

        ProductResult result = new ProductResult().convertFrom(product);

        // 补充Product信息
        fullFillProduct(result);
        return result;
    }

    @Override
    public PageResult<ProductResult> listProducts(QueryProductParam param, Pageable pageable) {
        if (contextHolder.isDeveloper()) {
            param.setPortalId(contextHolder.getPortal());
        }

        Page<Product> products = productRepository.findAll(buildSpecification(param), pageable);
        return new PageResult<ProductResult>().convertFrom(
                products, product -> {
                    ProductResult result = new ProductResult().convertFrom(product);
                    fullFillProduct(result);
                    return result;
                });
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
    public void publishProduct(String productId, String portalId) {
        portalService.hasPortal(portalId);
        if (publicationRepository.findByPortalIdAndProductId(portalId, productId).isPresent()) {
            return;
        }

        ProductPublication productPublication = new ProductPublication();
        productPublication.setPortalId(portalId);
        productPublication.setProductId(productId);

        publicationRepository.save(productPublication);
    }

    @Override
    public PageResult<ProductPublicationResult> getPublications(String productId, Pageable pageable) {
        Page<ProductPublication> publications = publicationRepository.findByProductId(productId, pageable);

        return new PageResult<ProductPublicationResult>().convertFrom(
                publications, publication -> {
                    ProductPublicationResult publicationResult = new ProductPublicationResult().convertFrom(publication);
                    PortalResult portal;
                    try {
                        portal = portalService.getPortal(publication.getPortalId());
                    } catch (Exception e) {
                        log.error("Failed to get portal: {}", publication.getPortalId(), e);
                        return null;
                    }

                    publicationResult.setPortalName(portal.getName());
                    publicationResult.setAutoApproveSubscription(portal.getPortalSettingConfig().getAutoApproveSubscriptions());

                    return publicationResult;
                });
    }

    @Override
    public void unpublishProduct(String productId, String portalId) {
        portalService.hasPortal(portalId);

        publicationRepository.findByPortalIdAndProductId(portalId, productId)
                .ifPresent(publicationRepository::delete);
    }

    public void unpublishProduct(String portalId) {
        publicationRepository.deleteAllByPortalId(portalId);
    }

    @Override
    public void deleteProduct(String productId) {
        Product Product = findProduct(productId);

        // 下线后删除
        publicationRepository.deleteByProductId(productId);
        productRepository.delete(Product);
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
        productRefRepository.findByProductId(product.getProductId())
                .ifPresent(productRef -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PRODUCT_REF, product.getProductId());
                });

        log.info("addProductRef - param: {}", param);
        
        ProductRef productRef = param.convertTo();
        log.info("addProductRef - after convertTo: {}", productRef);
        
        productRef.setProductId(productId);
        
        
        syncConfig(product, productRef);

        productRefRepository.save(productRef);
    }

    @Override
    public ProductRefResult getProductRef(String productId) {
        return productRefRepository.findFirstByProductId(productId)
                .map(productRef -> new ProductRefResult().convertFrom(productRef))
                .orElse(null);
    }


    @Override
    public void deleteProductRef(String productId) {
        ProductRef productRef = productRefRepository.findFirstByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_API_NOT_FOUND, productId));

        productRefRepository.delete(productRef);
    }

    private void syncConfig(Product product, ProductRef productRef) {
        SourceType sourceType = productRef.getSourceType();
        
        log.info("syncConfig - sourceType: {}, productRef: {}", sourceType, productRef);

        if (sourceType.isGateway()) {
            GatewayResult gateway = gatewayService.getGateway(productRef.getGatewayId());
            Object config = gateway.getGatewayType().isHigress() ? productRef.getHigressRefConfig() : productRef.getApigRefConfig();
            if (product.getType() == ProductType.REST_API) {
                String apiConfig = gatewayService.fetchAPIConfig(gateway.getGatewayId(), config);
                productRef.setApiConfig(apiConfig);
            } else {
                String mcpConfig = gatewayService.fetchMcpConfig(gateway.getGatewayId(), config);
                productRef.setMcpConfig(mcpConfig);
            }
        } else if (sourceType.isNacos()) {
            // 从Nacos获取MCP Server配置
            NacosRefConfig nacosRefConfig = productRef.getNacosRefConfig();
            log.info("syncConfig - nacosRefConfig: {}, nacosId: {}", nacosRefConfig, productRef.getNacosId());
            
            if (nacosRefConfig != null) {
                String mcpConfig = nacosService.fetchMcpConfig(productRef.getNacosId(), nacosRefConfig);
                productRef.setMcpConfig(mcpConfig);
                log.info("syncConfig - mcpConfig set: {}", mcpConfig);
            } else {
                log.warn("syncConfig - nacosRefConfig is null, cannot fetch MCP config");
            }
        }
        productRef.setEnabled(true);
    }

    private void fullFillProduct(ProductResult product) {
        productRefRepository.findFirstByProductId(product.getProductId())
                .ifPresent(productRef -> {
                    product.setEnabled(productRef.getEnabled());
                    if (StrUtil.isNotBlank(productRef.getApiConfig())) {
                        product.setApiConfig(JSONUtil.toBean(productRef.getApiConfig(), APIConfigResult.class));
                    }

                    // API Config
                    if (StrUtil.isNotBlank(productRef.getMcpConfig())) {
                        product.setMcpConfig(JSONUtil.toBean(productRef.getMcpConfig(), MCPConfigResult.class));
                    }
                    product.setStatus(ProductStatus.READY);
                });

        if (publicationRepository.existsByProductId(product.getProductId())) {
            product.setStatus(ProductStatus.PUBLISHED);
        }
    }

    private Product findPublishedProduct(String portalId, String productId) {
        ProductPublication publication = publicationRepository.findByPortalIdAndProductId(portalId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, Resources.PRODUCT, productId));

        return findProduct(publication.getProductId());
    }

    private Specification<Product> buildSpecification(QueryProductParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StrUtil.isNotBlank(param.getPortalId())) {
                Subquery<String> subquery = query.subquery(String.class);
                Root<ProductPublication> publicationRoot = subquery.from(ProductPublication.class);
                subquery.select(publicationRoot.get("productId"))
                        .where(cb.equal(publicationRoot.get("portalId"), param.getPortalId()));
                predicates.add(root.get("productId").in(subquery));
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

    @EventListener
    @Async("taskExecutor")
    @Override
    public void handlePortalDeletion(PortalDeletingEvent event) {
        String portalId = event.getPortalId();
        try {
            log.info("Starting to cleanup publications for portal {}", portalId);
            publicationRepository.deleteAllByPortalId(portalId);

            log.info("Completed cleanup publications for portal {}", portalId);
        } catch (Exception e) {
            log.error("Failed to cleanup developers for portal {}: {}", portalId, e.getMessage());
        }
    }

    @Override
    public Map<String, ProductResult> getProducts(List<String> productIds) {
        List<Product> products = productRepository.findByProductIdIn(productIds);
        return products.stream()
                .collect(Collectors.toMap(Product::getProductId, product -> new ProductResult().convertFrom(product)));
    }
}
