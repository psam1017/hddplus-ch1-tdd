package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class UserPointStreamRepository implements UserPointRepository {

    private final UserPointTable userPointTable;

    public UserPointStreamRepository(UserPointTable userPointTable) {
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
        return UserPointQueueHolder
                .search(id)
                .orElseGet(() -> userPointTable.selectById(id));
    }
}
