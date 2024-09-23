package io.hhplus.tdd.point;

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
