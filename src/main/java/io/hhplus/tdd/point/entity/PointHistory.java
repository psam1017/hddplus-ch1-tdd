package io.hhplus.tdd.point.entity;

import io.hhplus.tdd.point.enumeration.TransactionType;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

    public static PointHistory of(long userId, long amount, TransactionType type) {
        return new PointHistory(0, userId, amount, type, System.currentTimeMillis());
    }
}
