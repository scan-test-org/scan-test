package com.alibaba.apiopenplatform.support.consumer;

import com.alibaba.apiopenplatform.support.enums.CredentialMode;
import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class ApiKeyConfig {

    private List<ApiKeyCredential> credentials;

    /**
     * apikey的位置
     */
    private String source = "Default";

    /**
     * apikey参数名称
     */
    private String key = "Authorization";

    @Data
    public static class ApiKeyCredential {

        private String apiKey;

        private CredentialMode mode = CredentialMode.SYSTEM;
    }
}