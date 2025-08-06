package com.alibaba.apiopenplatform.dto.params.consumer;

import com.alibaba.apiopenplatform.support.enums.SubscriptionStatus;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class QuerySubscriptionParam {

    private SubscriptionStatus status;
}