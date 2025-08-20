package com.alibaba.apiopenplatform.support.common;

import com.alibaba.apiopenplatform.support.enums.UserType;
import lombok.Builder;
import lombok.Data;

/**
 * @author zh
 */
@Data
@Builder
public class User {

    private UserType userType;

    private String userId;
}
