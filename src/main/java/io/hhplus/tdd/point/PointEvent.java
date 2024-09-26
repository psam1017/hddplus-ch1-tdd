package io.hhplus.tdd.point;

import org.springframework.context.ApplicationEvent;

public class PointEvent extends ApplicationEvent {

    public PointEvent(Object source) {
        super(source);
    }
}
