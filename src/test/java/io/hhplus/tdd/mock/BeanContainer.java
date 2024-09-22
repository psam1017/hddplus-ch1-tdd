package io.hhplus.tdd.mock;

import io.hhplus.tdd.point.*;

public class BeanContainer {

    public final UserPointRepository userPointRepository;
    public final PointHistoryRepository pointHistoryRepository;
    public final PointService pointService;
    public final PointController pointController;

    public BeanContainer() {
        // mock objects
        this.userPointRepository = new UserPointMockRepository();
        this.pointHistoryRepository = new PointHistoryMockRepository();

        // systems under test
        this.pointService = new PointTableService(userPointRepository, pointHistoryRepository);
        this.pointController = new PointController(pointService);
    }
}
