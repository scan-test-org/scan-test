package com.alibaba.apiopenplatform.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author zh
 */
@Getter
public class DeveloperDeletingEvent extends ApplicationEvent {

    private final String developerId;

    public DeveloperDeletingEvent(String developerId) {
        super(developerId);
        this.developerId = developerId;
    }
}
