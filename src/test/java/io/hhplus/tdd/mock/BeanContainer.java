package io.hhplus.tdd.mock;

import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointHistoryRepository;
import io.hhplus.tdd.point.UserPointRepository;

public class BeanContainer {

    public final PointController pointController;
    public final UserPointRepository userPointRepository;
    public final PointHistoryRepository pointHistoryRepository;

    public BeanContainer() {
        // point bean
        this.userPointRepository = new UserPointMockRepository();
        this.pointHistoryRepository = new PointHistoryMockRepository();
        this.pointController = new PointController();

        // others ...
    }
}
