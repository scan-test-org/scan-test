package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.converter.APIGConfigConverter;
import com.alibaba.apiopenplatform.support.converter.HigressConfigConverter;
import com.alibaba.apiopenplatform.support.enums.GatewayType;
import com.alibaba.apiopenplatform.support.gateway.APIGConfig;
import com.alibaba.apiopenplatform.support.gateway.HigressConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_gateway",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"gateway_id"}, name = "uk_gateway_id"),
        })
@Data
public class Gateway extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gateway_name", length = 64, nullable = false)
    private String gatewayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 32, nullable = false)
    private GatewayType gatewayType;

    @Column(name = "gateway_id", length = 32, nullable = false)
    private String gatewayId;

    @Column(name = "admin_id", length = 64, nullable = false)
    private String adminId;

    @Convert(converter = APIGConfigConverter.class)
    @Column(name = "apig_config", columnDefinition = "text")
    private APIGConfig apigConfig;

    @Convert(converter = HigressConfigConverter.class)
    @Column(name = "higress_config", columnDefinition = "text")
    private HigressConfig higressConfig;
}
