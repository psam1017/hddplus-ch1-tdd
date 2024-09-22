package io.hhplus.tdd.point;

import org.springframework.stereotype.Repository;

@Repository
public class UserPointTableRepository implements UserPointRepository {

    @Override
    public UserPoint save(UserPoint userPoint) {
        return null;
    }

    @Override
    public UserPoint selectById(long id) {
        return null;
    }
}
