package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entity.PointHistory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointHistoryTableRepository implements PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    public PointHistoryTableRepository(PointHistoryTable pointHistoryTable) {
        this.pointHistoryTable = pointHistoryTable;
    }

    @Override
    public PointHistory save(PointHistory pointHistory) {
        return pointHistoryTable.insert(
                pointHistory.userId(),
                pointHistory.amount(),
                pointHistory.type(),
                pointHistory.updateMillis()
        );
    }

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
