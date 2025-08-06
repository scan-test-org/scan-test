package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.converter.ApiKeyConfigConverter;
import com.alibaba.apiopenplatform.converter.HmacConfigConverter;
import com.alibaba.apiopenplatform.converter.JwtConfigConverter;
import com.alibaba.apiopenplatform.support.consumer.ApiKeyConfig;
import com.alibaba.apiopenplatform.support.consumer.HmacConfig;
import com.alibaba.apiopenplatform.support.consumer.JwtConfig;
import lombok.Data;

import javax.persistence.*;

/**
 * @author zh
 */

@Entity
@Table(name = "consumer_credential",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"consumer_id"}, name = "uk_consumer_id")
        })
@Data
public class ConsumerCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_id", nullable = false)
    private String consumerId;

    @Column(name = "apikey_config", columnDefinition = "text")
    @Convert(converter = ApiKeyConfigConverter.class)
    private ApiKeyConfig apiKeyConfig;

    @Column(name = "hmac_config", columnDefinition = "text")
    @Convert(converter = HmacConfigConverter.class)
    private HmacConfig hmacConfig;

    @Column(name = "jwt_config", columnDefinition = "text")
    @Convert(converter = JwtConfigConverter.class)
    private JwtConfig jwtConfig;
}
