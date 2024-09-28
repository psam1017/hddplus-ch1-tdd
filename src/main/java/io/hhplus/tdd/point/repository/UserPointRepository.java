package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import org.springframework.stereotype.Repository;

@Repository
public class UserPointRepository {

    private final UserPointTable userPointTable;

    public UserPointRepository(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    public UserPoint save(UserPoint userPoint) {
        return userPointTable.insertOrUpdate(
                userPoint.id(),
                userPoint.point()
        );
    }

    public UserPoint selectById(long id) {
        return userPointTable.selectById(id);
    }
}
