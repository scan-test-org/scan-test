package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_product_publishment")
@NamedEntityGraph(
        name = "product.properties",
        attributeNodes = @NamedAttributeNode("product")
)
@Data
public class ProductPublication extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_id", length = 32, nullable = false)
    private String portalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", updatable = false)
    private Product product;
}
