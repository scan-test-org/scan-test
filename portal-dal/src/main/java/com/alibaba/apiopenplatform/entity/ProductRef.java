package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.converter.APIGRefConfigConverter;
import com.alibaba.apiopenplatform.converter.HigressRefConfigConverter;
import com.alibaba.apiopenplatform.converter.NacosRefConfigConverter;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import com.alibaba.apiopenplatform.support.product.APIGRefConfig;
import com.alibaba.apiopenplatform.support.product.HigressRefConfig;
import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_ref")
@Data
public class ProductRef extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;

    @Column(name = "gateway_id", length = 32)
    private String gatewayId;

    @Column(name = "apig_ref_config", columnDefinition = "text")
    @Convert(converter = APIGRefConfigConverter.class)
    private APIGRefConfig apigRefConfig;

    @Column(name = "higress_ref_config", columnDefinition = "text")
    @Convert(converter = HigressRefConfigConverter.class)
    private HigressRefConfig higressRefConfig;

    @Column(name = "nacos_id", length = 32)
    private String nacosId;

    @Column(name = "nacos_ref_config", columnDefinition = "text")
    @Convert(converter = NacosRefConfigConverter.class)
    private NacosRefConfig nacosRefConfig;

    @Column(name = "source_type", length = 32)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "api_spec", columnDefinition = "text")
    private String apiSpec;

    @Column(name = "mcp_spec", columnDefinition = "text")
    private String mcpSpec;

    @Column(name = "enabled")
    private Boolean enabled;
}
