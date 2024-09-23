package io.hhplus.tdd.infrastructure;

public class UniqueUserIdHolder {

    private static long id = 0;

    public static long next() {
        return ++id;
    }
}
