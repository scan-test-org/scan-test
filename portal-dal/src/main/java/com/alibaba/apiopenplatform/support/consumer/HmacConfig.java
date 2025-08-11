package com.alibaba.apiopenplatform.support.consumer;

import com.alibaba.apiopenplatform.support.enums.CredentialMode;
import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class HmacConfig {

    private List<HmacCredential> credentials;

    @Data
    public static class HmacCredential {
        private String ak;
        private String sk;

        private CredentialMode mode;
    }
}
