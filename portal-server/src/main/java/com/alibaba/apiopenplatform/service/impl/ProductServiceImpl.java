package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.alibaba.apiopenplatform.service.NacosService;
import com.alibaba.apiopenplatform.support.enums.ProductStatus;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import javax.persistence.criteria.*;

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

    private final NacosService nacosService;

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
                    PortalResult portal = portalService.getPortal(publication.getPortalId());

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

    @Override
    public void deleteProduct(String productId) {
        Product Product = findProduct(productId);

        // 下线后删除
        publicationRepository.deleteByProductId(productId);
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
        productRefRepository.findByProductId(product.getProductId())
                .ifPresent(productRef -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PRODUCT_REF, product.getProductId());
                });

        ProductRef productRef = param.convertTo();
        productRef.setProductId(productId);
        syncSpec(product, productRef);

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

    private void fullFillAPISpec(ProductResult product) {
//        productRefRepository.findFirstByProductId(product.getProductId())
//                .map(productRef -> {
//                    try {
//
//                        // 来源于网关
//                        if (productRef.getSourceType().isGateway()) {
//                            GatewayResult gateway = gatewayService.getGateway(productRef.getGatewayId());
//                            ProductType type = product.getType();
//
//                            APIGRefConfig apigRefConfig = productRef.getApigRefConfig();
//
//                            if (type == ProductType.REST_API) {
//                                String apiSpec = gatewayService.fetchAPISpec(productRef.getGatewayId(), , apigRefConfig.getApiId());
//                                product.setApiSpec(apiSpec);
//                            }
//                        }
//
//
//                        APIGRefConfig apigRefConfig = productRef.getApigRefConfig();
//
//                        ProductType type = product.getType();
//                        if (type == ProductType.REST_API) {
//                            String apiSpec = gatewayService.fetchAPISpec(productRef.getGatewayId(), , apigRefConfig.getApiId());
//                            product.setApiSpec(apiSpec);
//                        } else if (type == ProductType.MCP_SERVER) {
//                            String mcpSpec;
//                            if (productRef.getSourceType() == SourceType.GATEWAY) {
//                                mcpSpec = gatewayService.fetchMcpSpec(productRef.getGatewayId(), apigRefConfig.getApiId(), apigRefConfig.getMcpRouteId(), routeConfig.getName());
//                            } else if (productRef.getSourceType() == SourceType.NACOS) {
//                                // 从Nacos获取MCP Server详情
//                                // 对于Nacos，apiId字段存储的是mcpServerName
//                                String mcpServerName = productRef.getApiId();
//                                String namespaceId = "public"; // 暂时使用默认namespace
//                                String version = null; // 暂时使用最新版本
//
//                                McpMarketDetailParam detailParam = nacosService.getMcpServerDetail(productRef.getNacosId(), mcpServerName, namespaceId, version);
//                                // 将详情转换为JSON字符串
//                                mcpSpec = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(detailParam);
//                            } else {
//                                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的数据源类型: " + productRef.getSourceType());
//                            }
//                            product.setMcpSpec(mcpSpec);
//                        }
//                        product.setStatus(ProductStatus.ENABLE);
//                    } catch (Exception e) {
//                        log.error("Failed to fetch API spec for product: {}, sourceType: {}, apiId: {}",
//                                product.getProductId(), productRef.getSourceType(), productRef.getApiId(), e);
//                        product.setStatus(ProductStatus.DISABLE);
//                    }
//                    return product;
//                })
//                .orElseGet(() -> {
//                    product.setStatus(ProductStatus.PENDING);
//                    return product;
//                });
    }

    private void syncSpec(Product product, ProductRef productRef) {
        SourceType sourceType = productRef.getSourceType();

        if (sourceType.isGateway()) {
            GatewayResult gateway = gatewayService.getGateway(productRef.getGatewayId());
            Object config = gateway.getGatewayType().isHigress() ? productRef.getHigressRefConfig() : productRef.getApigRefConfig();
            if (product.getType() == ProductType.REST_API) {
                String apiSpec = gatewayService.fetchAPISpec(gateway.getGatewayId(), config);
                productRef.setApiSpec(apiSpec);
            } else {
                String mcpSpec = gatewayService.fetchMcpSpec(gateway.getGatewayId(), config);
                productRef.setMcpSpec(mcpSpec);
            }
        } else if (sourceType.isNacos()) {
            // 从Nacos获取MCP Server详情
            NacosRefConfig nacosRefConfig = productRef.getNacosRefConfig();
            if (nacosRefConfig != null) {
                try {
                    String mcpServerName = nacosRefConfig.getMcpServerName();
                    String namespaceId = nacosRefConfig.getNamespaceId() != null ? nacosRefConfig.getNamespaceId() : "public";
                    String version = nacosRefConfig.getVersion();

                    // 获取MCP Server详情
                    com.alibaba.apiopenplatform.dto.params.mcp.McpMarketDetailParam detailParam =
                        nacosService.getMcpServerDetail(productRef.getNacosId(), mcpServerName, namespaceId, version);

                    // 将详情转换为JSON字符串
                    String mcpSpec = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(detailParam);
                    productRef.setMcpSpec(mcpSpec);
                } catch (Exception e) {
                    log.error("Failed to fetch MCP spec from Nacos for product: {}, nacosId: {}, mcpServerName: {}",
                            product.getProductId(), productRef.getNacosId(), nacosRefConfig.getMcpServerName(), e);
                    // 不抛出异常，让流程继续，只是不设置mcpSpec
                }
            }
        }
        productRef.setEnabled(true);
    }

    private void fullFillProduct(ProductResult product) {
        productRefRepository.findFirstByProductId(product.getProductId())
                .ifPresent(productRef -> {
                    product.setEnabled(productRef.getEnabled());
                    product.setApiSpec(productRef.getApiSpec());

                    // Spec
                    if (StrUtil.isNotBlank(productRef.getMcpSpec())) {
                        product.setMcpSpec(JSONUtil.toBean(productRef.getMcpSpec(), MCPServerResult.class));
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
}
