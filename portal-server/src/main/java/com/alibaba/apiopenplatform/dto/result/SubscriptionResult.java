package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ProductSubscription;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zh
 */
@Data
public class SubscriptionResult implements OutputConverter<SubscriptionResult, ProductSubscription> {

    private String productId;

    private String consumerId;

    private String status;

    private LocalDateTime createAt;

    private LocalDateTime updatedAt;
}