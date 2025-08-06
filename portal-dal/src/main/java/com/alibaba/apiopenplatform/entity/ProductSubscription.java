package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.enums.SubscriptionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@Entity
@Table(name = "product_subscription",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "consumer_id"}, name = "uk_product_consumer")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", length = 32, nullable = false)
    private String productId;

    @Column(name = "consumer_id", length = 32, nullable = false)
    private String consumerId;

    @Column(name = "developer_id", length = 32, nullable = false)
    private String developerId;

    @Column(name = "portal_id", length = 32)
    private String portalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private SubscriptionStatus status;
}