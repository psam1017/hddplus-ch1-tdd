package io.hhplus.tdd.point.entity;

import io.hhplus.tdd.point.exception.MaxPointExceededException;
import io.hhplus.tdd.point.exception.OutOfPointException;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    private static final long MAX_POINT = 100000;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    // TODO: 2024-09-28 domain test
    public UserPoint charge(long amount) {
        if (this.point + amount > MAX_POINT) {
            throw new MaxPointExceededException(MAX_POINT);
        }
        return new UserPoint(id, point + amount, 0);
    }

    public UserPoint use(long amount) {
        if (this.point < amount) {
            throw new OutOfPointException(id, this.point);
        }
        return new UserPoint(id, point - amount, 0);
    }
}
