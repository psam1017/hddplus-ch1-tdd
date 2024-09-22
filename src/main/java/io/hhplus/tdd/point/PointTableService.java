package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointTableService implements PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointTableService(UserPointRepository userPointRepository, PointHistoryRepository pointHistoryRepository) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @Override
    public UserPoint point(long id) {
        return new UserPoint(0, 0, 0);
    }

    @Override
    public List<PointHistory> history(long id) {
        return List.of();
    }

    @Override
    public UserPoint charge(long id, long amount) {
        return new UserPoint(0, 0, 0);
    }

    @Override
    public UserPoint use(long id, long amount) {
        return new UserPoint(0, 0, 0);
    }
}
