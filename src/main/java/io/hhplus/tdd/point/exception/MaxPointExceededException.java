package io.hhplus.tdd.point.exception;

public class MaxPointExceededException extends RuntimeException {

    public MaxPointExceededException(long currentPoint) {
        super("user can have only %d points".formatted(currentPoint));
    }
}
