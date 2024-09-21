package io.hhplus.tdd.unit;

import io.hhplus.tdd.mock.BeanContainer;

public abstract class TddApplicationUnitTest {

    private BeanContainer beanContainer;

    protected BeanContainer getBeanContainer() {
        if (beanContainer == null) {
            beanContainer = new BeanContainer();
        }
        return beanContainer;
    }
}
