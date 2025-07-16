package com.alibaba.apiopenplatform.support.consumer;

import lombok.Data;

/**
 * @author zh
 */
@Data
class HmacIdentityConfig {

    private String type;

    private String generateMode;

    private String ak;

    private String sk;
}
