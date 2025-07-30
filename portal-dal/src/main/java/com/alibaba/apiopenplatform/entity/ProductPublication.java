package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "publication")
@Data
public class ProductPublication extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_id", length = 32, nullable = false)
    private String portalId;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;
}
