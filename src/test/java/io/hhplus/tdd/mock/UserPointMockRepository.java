package io.hhplus.tdd.mock;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.UserPointRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class UserPointMockRepository implements UserPointRepository {

    private final AtomicLong autoIncrementId = new AtomicLong(0);
    private final List<UserPoint> data = Collections.synchronizedList(new ArrayList<>());

    @Override
    public UserPoint save(UserPoint userPoint) {
        if (userPoint.id() <= 0) {
            UserPoint newUserPoint = new UserPoint(autoIncrementId.incrementAndGet(), userPoint.point(), userPoint.updateMillis());
            data.add(newUserPoint);
            return newUserPoint;
        } else {
            data.removeIf(up -> up.id() == userPoint.id());
            data.add(userPoint);
            return userPoint;
        }
    }

    @Override
    public UserPoint selectById(long id) {
        return data.stream().filter(up -> up.id() == id).findAny().orElse(UserPoint.empty(id));
    }
}
