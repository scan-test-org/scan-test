package com.alibaba.apiopenplatform.entity;

import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Table(name = "t_apim_product_setting", 
        indexes = {
                @Index(name = "idx_product_id", columnList = "product_id")
        })
@Data
@Entity
public class ProductSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", unique = true)
    private String productId;

    @Column(name = "api_list", length = 256)
    private String apiList; // 可用 JSON 存储

    @Column(name = "auth_type", length = 32)
    private String authType; // 认证方式

    @Column(name = "rate_limit", length = 32)
    private String rateLimit; // 限流策略
}
