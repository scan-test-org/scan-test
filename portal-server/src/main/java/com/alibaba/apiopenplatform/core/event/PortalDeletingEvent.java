package com.alibaba.apiopenplatform.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author zh
 */
@Getter
public class PortalDeletingEvent extends ApplicationEvent {

    private final String portalId;

    public PortalDeletingEvent(String portalId) {
        super(portalId);
        this.portalId = portalId;
    }
}
