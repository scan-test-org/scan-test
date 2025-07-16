package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
/**
 * Product Version Entity
 * @author zh
 */
@Entity
@Table(name = "product_version")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVersion extends BaseEntity {

    @Id
    private String versionId;

    private String productId;

    private String version;

    private String description;

    private String status;

    private String apiList;

    private String authType;

    private String rateLimit;
} 