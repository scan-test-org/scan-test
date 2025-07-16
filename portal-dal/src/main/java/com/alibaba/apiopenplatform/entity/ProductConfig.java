package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * Product Config Entity
 * @author zh
 */
@Entity
@Table(name = "product_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductConfig extends BaseEntity {

    @Id
    private String configId;

    private String productId;

    private String configKey;

    private String configValue;

    private String description;
} 