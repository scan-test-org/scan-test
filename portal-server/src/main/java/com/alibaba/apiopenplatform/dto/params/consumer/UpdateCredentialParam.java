package com.alibaba.apiopenplatform.dto.params.consumer;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ConsumerCredential;
import com.alibaba.apiopenplatform.support.consumer.ApiKeyConfig;
import com.alibaba.apiopenplatform.support.consumer.HmacConfig;
import com.alibaba.apiopenplatform.support.consumer.JwtConfig;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class UpdateCredentialParam implements InputConverter<ConsumerCredential> {

    private ApiKeyConfig apiKeyConfig;

    private HmacConfig hmacConfig;

    private JwtConfig jwtConfig;
}
