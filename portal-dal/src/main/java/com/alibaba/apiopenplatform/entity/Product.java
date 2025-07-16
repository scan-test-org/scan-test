package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * Product Entity
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_product",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id"}, name = "uk_product_id"),
                @UniqueConstraint(columnNames = {"name", "owner_id"}, name = "uk_name_owner_id")
        })
@NamedEntityGraph(
        name = "product.properties",
        attributeNodes = {
                @NamedAttributeNode("productSetting")
        }
)
@Data
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;

    @Column(name = "portal_id", length = 32)
    private String portalId;

    @Column(name = "owner_id", length = 32)
    private String ownerId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "admin_id", length = 32)
    private String adminId;

    @Column(name = "status", length = 32)
    private String status; // PUBLISHED, DIS

    @Column(name = "enable_consumer_auth", length = 32)
    private String enableConsumerAuth; // 是否启用消费者鉴权

    @Column(name = "type", length = 32)
    private String type; // REST_API, HTTP_API, MCP_SERVER, ROUTE, ROUTE_GROUP

    @Column(name = "document", length = 1024)
    private String document; // 自定义说明文档

    @Column(name = "icon", length = 256)
    private String icon; // 自定义图标

    @Column(name = "category", length = 64)
    private String category; // 自定义分类

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private ProductSetting productSetting;
}