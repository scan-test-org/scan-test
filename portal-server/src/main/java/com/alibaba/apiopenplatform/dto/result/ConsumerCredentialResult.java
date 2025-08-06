package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ConsumerCredential;
import com.alibaba.apiopenplatform.support.consumer.ApiKeyConfig;
import com.alibaba.apiopenplatform.support.consumer.HmacConfig;
import com.alibaba.apiopenplatform.support.consumer.JwtConfig;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class ConsumerCredentialResult implements OutputConverter<ConsumerCredentialResult, ConsumerCredential> {

    private JwtConfig jwtConfig;

    private HmacConfig hmacConfig;

    private ApiKeyConfig apiKeyConfig;
}
