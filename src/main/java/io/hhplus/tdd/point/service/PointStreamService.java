package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.infrastructure.PointEvent;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumeration.TransactionType;
import io.hhplus.tdd.point.exception.RequestPointNotPositiveException;
import io.hhplus.tdd.point.exception.MaxPointExceededException;
import io.hhplus.tdd.point.exception.OutOfPointException;
import io.hhplus.tdd.point.infrastructure.PointHistoryQueueHolder;
import io.hhplus.tdd.point.infrastructure.UserPointQueueHolder;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
public class PointStreamService implements PointService {

    private static final long MAX_POINT = 100000L; // 100,000 포인트

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PointStreamService(UserPointRepository userPointRepository, PointHistoryRepository pointHistoryRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public UserPoint point(long id) {
        return userPointRepository.selectById(id);
    }

    @Override
    public List<PointHistory> history(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    @Override
    public UserPoint charge(long id, long amount) {
        // 유효성 검사 - 충전 포인트는 0보다 커야 합니다.
        if (amount <= 0) {
            throw new RequestPointNotPositiveException();
        }

        // 포인트 충전
        UserPoint userPoint = userPointRepository.selectById(id);
        userPoint = userPoint.charge(amount);

        // 로직 검사 - 최대 보유 가능 포인트를 제한합니다.
        if (userPoint.point() > MAX_POINT) {
            throw new MaxPointExceededException(MAX_POINT);
        }

        // 포인트를 Queue 에 저장합니다.
        UserPointQueueHolder.add(userPoint);

        // 충전 내역을 Queue 에 저장합니다.
        // PointHistoryTable 에서는 pointHistoryId 와 updateMillis 가 버려지기 때문에 아무 값이나 입력합니다.
        PointHistoryQueueHolder.add(new PointHistory(0, id, amount, TransactionType.CHARGE, 0));

        // PointEvent 를 발생시켜 PointStreamImpl 에게 Queue 를 비우도록 요청합니다.
        applicationEventPublisher.publishEvent(new PointEvent(this));

        return userPoint;
    }

    @Override
    public UserPoint use(long id, long amount) {
        // 유효성 검사 - 사용 포인트는 0보다 커야 합니다.
        if (amount <= 0) {
            throw new RequestPointNotPositiveException();
        }

        // 로직 검사 - 사용 포인트는 보유 포인트보다 작거나 같아야 합니다.
        UserPoint userPoint = userPointRepository.selectById(id);
        if (userPoint.point() < amount) {
            throw new OutOfPointException(id, userPoint.point());
        }

        // 포인트 사용을 Queue 에 저장합니다.
        userPoint = userPoint.use(amount);
        UserPointQueueHolder.add(userPoint);

        // 사용 내역을 Queue 에 저장합니다.
        // PointHistoryTable 에서는 pointHistoryId 와 updateMillis 가 버려지기 때문에 아무 값이나 입력합니다.
        PointHistoryQueueHolder.add(new PointHistory(0, id, amount, TransactionType.USE, 0));

        // PointEvent 를 발생시켜 PointStreamImpl 에게 Queue 를 비우도록 요청합니다.
        applicationEventPublisher.publishEvent(new PointEvent(this));

        return userPoint;
    }
}
