package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@Entity
@Table(name = "consumerRef",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"gw_consumer_id"}, name = "uk_consumer_id"),
                @UniqueConstraint(columnNames = {"consumer_id", "region", "gateway_type"},
                        name = "uk_consumer_relation")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumerRef extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_id", length = 32, nullable = false)
    private String consumerId;

    @Column(name = "region", length = 32, nullable = false)
    private String region;

    @Column(name = "gateway_type", length = 32, nullable = false)
    private String gatewayType;

    @Column(name = "gw_consumer_id", length = 32, nullable = false)
    private String gwConsumerId;
}
