package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.entity.UserPoint;

public interface UserPointRepository {

    UserPoint save(UserPoint userPoint);

    UserPoint selectById(long id);
}
