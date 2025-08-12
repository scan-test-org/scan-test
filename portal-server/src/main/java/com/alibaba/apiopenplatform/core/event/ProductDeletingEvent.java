package com.alibaba.apiopenplatform.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author zh
 */
@Getter
public class ProductDeletingEvent extends ApplicationEvent {

    private final String productId;

    public ProductDeletingEvent(String productId) {
        super(productId);
        this.productId = productId;
    }
}
