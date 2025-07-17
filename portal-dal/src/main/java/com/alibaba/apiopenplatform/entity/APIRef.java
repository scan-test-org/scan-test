package com.alibaba.apiopenplatform.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author zh
 */
@Entity
@Table(name = "t_apim_api_ref",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"api_id"}, name = "uk_api_id")
        })
@Data
public class APIRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_id", length = 32, nullable = false)
    private String apiId;

    @Column(name = "gateway_id", length = 32, nullable = false)
    private String gatewayId;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;
}
