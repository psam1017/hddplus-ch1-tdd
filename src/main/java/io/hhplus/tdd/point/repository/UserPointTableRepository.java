package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import org.springframework.stereotype.Repository;

@Repository
public class UserPointTableRepository implements UserPointRepository {

    private final UserPointTable userPointTable;

    public UserPointTableRepository(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    @Override
    public UserPoint save(UserPoint userPoint) {
        return userPointTable.insertOrUpdate(
                userPoint.id(),
                userPoint.point()
        );
    }

    @Override
    public UserPoint selectById(long id) {
        return userPointTable.selectById(id);
    }
}
