package io.hhplus.tdd.point.exception;

public class ChargePointNotPositiveException extends RuntimeException {

    public ChargePointNotPositiveException() {
        super("Charge point must be positive");
    }
}
