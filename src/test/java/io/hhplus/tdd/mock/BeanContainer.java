package io.hhplus.tdd.mock;

import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.PointTableService;

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
