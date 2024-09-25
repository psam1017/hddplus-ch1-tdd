package io.hhplus.tdd.mock;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PointHistoryMockRepository implements PointHistoryRepository {

    private final AtomicLong autoIncrementId = new AtomicLong(0);
    private final List<PointHistory> data = Collections.synchronizedList(new ArrayList<>());

    @Override
    public PointHistory save(PointHistory pointHistory) {
        if (pointHistory.id() <= 0) {
            PointHistory newPointHistory = new PointHistory(autoIncrementId.incrementAndGet(), pointHistory.userId(), pointHistory.amount(), pointHistory.type(), pointHistory.updateMillis());
            data.add(newPointHistory);
            return newPointHistory;
        } else {
            data.removeIf(ph -> ph.id() == pointHistory.id());
            data.add(pointHistory);
            return pointHistory;
        }
    }

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return data.stream().filter(ph -> ph.userId() == userId).toList();
    }
}
