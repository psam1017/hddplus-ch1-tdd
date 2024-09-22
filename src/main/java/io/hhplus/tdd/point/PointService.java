package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {

    UserPoint point(long id);

    List<PointHistory> history(long id);

    UserPoint charge(long id, long amount);

    UserPoint use(long id, long amount);
}
