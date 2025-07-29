package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.converter.StringListConverter;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import com.alibaba.apiopenplatform.support.product.RouteConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_ref",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"api_id"}, name = "uk_api_id")
        })
@Data
public class ProductRef extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_id", length = 32, nullable = false)
    private String apiId;

    @Column(name = "gateway_id", length = 32)
    private String gatewayId;

    @Column(name = "nacos_id", length = 32)
    private String nacosId;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;

    @Column(name = "routes", columnDefinition = "text")
    @Convert(converter = StringListConverter.class)
    private List<RouteConfig> routes;

    @Column(name = "operations", columnDefinition = "text")
    @Convert(converter = StringListConverter.class)
    private List<String> operations;

    @Column(name = "type", length = 32, nullable = false)
    private ProductType type;
}
