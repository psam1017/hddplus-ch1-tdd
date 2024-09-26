package io.hhplus.tdd.point.stream;

import io.hhplus.tdd.point.infrastructure.PointEvent;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.infrastructure.UserPointIdentityMap;
import io.hhplus.tdd.point.infrastructure.PointHistoryQueueHolder;
import io.hhplus.tdd.point.infrastructure.UserPointQueueHolder;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PointStreamImpl implements PointStream {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointRepository userPointRepository;

    public PointStreamImpl(PointHistoryRepository pointHistoryRepository, UserPointRepository userPointRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
        this.userPointRepository = userPointRepository;
    }

    @Async("pointEventExecutor")
    @EventListener(PointEvent.class)
    @Override
    public void flush() {
        while (!UserPointQueueHolder.isEmpty()) {
            UserPoint userPoint = UserPointQueueHolder.poll();
            if (userPoint != null) {
                userPointRepository.save(userPoint);
                UserPointIdentityMap.remove(userPoint);
            }
        }
        while (!PointHistoryQueueHolder.isEmpty()) {
            PointHistory pointHistory = PointHistoryQueueHolder.poll();
            if (pointHistory != null) {
                pointHistoryRepository.save(pointHistory);
            }
        }
    }
}
