package io.hhplus.tdd.point.infrastructure;

import org.springframework.context.ApplicationEvent;

public class PointEvent extends ApplicationEvent {

    public PointEvent(Object source) {
        super(source);
    }
}
