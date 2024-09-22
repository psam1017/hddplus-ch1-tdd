package io.hhplus.tdd.point;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointHistoryTableRepository implements PointHistoryRepository {

    @Override
    public PointHistory save(PointHistory pointHistory) {
        return null;
    }

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return null;
    }
}
