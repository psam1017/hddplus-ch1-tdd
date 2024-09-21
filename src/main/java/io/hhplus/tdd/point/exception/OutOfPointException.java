package io.hhplus.tdd.point.exception;

public class OutOfPointException extends RuntimeException {

    public OutOfPointException(long userId, long currentPoint) {
        super("user %d has only %d points".formatted(userId, currentPoint));
    }
}
