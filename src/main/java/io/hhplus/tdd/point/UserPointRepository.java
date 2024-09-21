package io.hhplus.tdd.point;

public interface UserPointRepository {

    UserPoint save(UserPoint userPoint);

    UserPoint selectById(long id);
}
