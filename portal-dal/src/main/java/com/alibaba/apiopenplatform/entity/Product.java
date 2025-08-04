package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.enums.ProductType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id"}, name = "uk_product_id"),
                @UniqueConstraint(columnNames = {"name"}, name = "uk_name")
        })
@Data
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;

    @Column(name = "admin_id", length = 32)
    private String adminId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "type", length = 32)
    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "enable_consumer_auth")
    private Boolean enableConsumerAuth;

    @Column(name = "document", columnDefinition = "text")
    private String document;

    @Column(name = "icon", length = 256)
    private String icon;

    @Column(name = "category", length = 64)
    private String category;
}