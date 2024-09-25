package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.infrastructure.UserPointIdentityMap;
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
        return UserPointIdentityMap
                .find(id)
                .orElseGet(() -> userPointTable.selectById(id));
    }
}
