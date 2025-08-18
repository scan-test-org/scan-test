package com.alibaba.apiopenplatform.dto.params.consumer;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.ConsumerCredential;
import com.alibaba.apiopenplatform.support.consumer.ApiKeyConfig;
import com.alibaba.apiopenplatform.support.consumer.HmacConfig;
import com.alibaba.apiopenplatform.support.consumer.JwtConfig;
import lombok.Data;

import javax.validation.constraints.AssertTrue;

/**
 * @author zh
 */
@Data
public class CreateCredentialParam implements InputConverter<ConsumerCredential> {

    private ApiKeyConfig apiKeyConfig;

    private HmacConfig hmacConfig;

    private JwtConfig jwtConfig;

    @AssertTrue(message = "凭证信息不能为空")
    private boolean isValid() {
        return apiKeyConfig != null || hmacConfig != null || jwtConfig != null;
    }
}
