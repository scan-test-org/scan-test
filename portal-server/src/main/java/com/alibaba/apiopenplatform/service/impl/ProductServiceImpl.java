package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.apiopenplatform.core.constant.Resources;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.core.utils.IdGenerator;
import com.alibaba.apiopenplatform.dto.params.product.*;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketDetailParam;
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
import com.alibaba.apiopenplatform.support.product.APIGRefConfig;
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

        // 如果是MCP_SERVER类型，自动创建ProductRef关联数据源
//        if (param.getType() == ProductType.MCP_SERVER) {
//            if (param.getSourceType() == null || param.getMcpServerName() == null) {
//                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "MCP_SERVER类型必须指定sourceType和mcpServerName");
//            }
//
//            // 验证数据源配置
//            if (param.getSourceType() == SourceType.GATEWAY && param.getGatewayId() == null) {
//                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "GATEWAY类型必须指定gatewayId");
//            }
//            if (param.getSourceType() == SourceType.NACOS && param.getNacosId() == null) {
//                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "NACOS类型必须指定nacosId");
//            }
//
//            CreateProductRefParam refParam = new CreateProductRefParam();
//            refParam.setApiId(param.getMcpServerName()); // 使用MCP Server名称作为apiId
//            refParam.setSourceType(param.getSourceType());
//
//            // 根据数据源类型设置相应的ID
//            if (param.getSourceType() == SourceType.GATEWAY) {
//                refParam.setGatewayId(param.getGatewayId());
//            } else if (param.getSourceType() == SourceType.NACOS) {
//                refParam.setNacosId(param.getNacosId());
//            }
//
//            // 创建路由配置
//            RouteOption routeOption = new RouteOption();
//            routeOption.setRouteId(param.getMcpServerName());
//            routeOption.setName(param.getMcpServerName());
//            refParam.setRoutes(java.util.Arrays.asList(routeOption));
//
//            addProductRef(productId, refParam);
//        }

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
        if (publicationRepository.findByPortalIdAndProductId(param.getPortalId(), productId).isPresent()) {
            return;
        }

        ProductPublication productPublication = param.convertTo();

        publicationRepository.save(productPublication);
    }

    @Override
    public void unpublishProduct(String productId, UnPublishProductParam param) {
        portalService.hasPortal(param.getPortalId());

        publicationRepository.findByPortalIdAndProductId(param.getPortalId(), productId)
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
        productRefRepository.findByProductId(product.getProductId())
                .ifPresent(productRef -> {
                    throw new BusinessException(ErrorCode.RESOURCE_EXIST, Resources.PRODUCT_REF, product.getProductId());
                });

        ProductRef productRef = param.convertTo();
        productRef.setProductId(productId);
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

    private void fullFillSpec(Product product, ProductRef productRef) {
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
        } else {

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
