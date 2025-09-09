package com.alibaba.apiopenplatform.support.portal;

import lombok.Data;

/**
 * @author zh
 */
@Data
public class JwtBearerConfig {

    /**
     * JWT公钥
     */
    private String publicKeys;
}
