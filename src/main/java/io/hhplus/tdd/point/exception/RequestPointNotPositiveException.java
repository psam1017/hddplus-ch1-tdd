package io.hhplus.tdd.point.exception;

public class RequestPointNotPositiveException extends RuntimeException {

    public RequestPointNotPositiveException() {
        super("Charge/Use point must be positive");
    }
}
