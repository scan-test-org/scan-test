package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.enums.GatewayType;
import lombok.*;

import javax.persistence.*;

/**
 * @author zh
 */
@Entity
@Table(name = "consumer_ref")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConsumerRef extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_id", length = 64, nullable = false)
    private String consumerId;

    @Column(name = "gateway_type", length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private GatewayType gatewayType;

    @Column(name = "gw_consumer_id", length = 64, nullable = false)
    private String gwConsumerId;

    @Column(name = "gateway_identity", columnDefinition = "text", nullable = false)
    private String gatewayIdentity;
}
