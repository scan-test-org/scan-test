package com.alibaba.apiopenplatform.dto.result;

import lombok.Builder;
import lombok.Data;

/**
 * @author zh
 */
@Data
@Builder
public class AuthResult {

    private String accessToken;

    private String expireTime;
}
