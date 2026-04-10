package com.windpower.diag.event;

import org.noear.solon.annotation.Component;
import org.noear.solon.core.event.EventBus;

@Component
public class EventPublisher {
    public void publish(Object event) {
        EventBus.publish(event);
    }
}
