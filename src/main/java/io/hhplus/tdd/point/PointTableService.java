package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.ChargePointNotPositiveException;
import io.hhplus.tdd.point.exception.MaxPointExceededException;
import io.hhplus.tdd.point.exception.OutOfPointException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointTableService implements PointService {

    private static final long MAX_POINT = 100000L; // 100,000 포인트

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointTableService(UserPointRepository userPointRepository, PointHistoryRepository pointHistoryRepository) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    // TODO: 2024-09-22 동시성 제어 리팩토링

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
            throw new ChargePointNotPositiveException();
        }

        // 포인트 충전
        UserPoint userPoint = userPointRepository.selectById(id);
        userPoint = userPoint.charge(amount);

        // 로직 검사 - 최대 보유 가능 포인트를 제한합니다.
        if (userPoint.point() > MAX_POINT) {
            throw new MaxPointExceededException(MAX_POINT);
        }
        userPointRepository.save(userPoint);

        // 충전 내역 저장
        // PointHistoryTable 에서는 pointHistoryId 와 updateMillis 가 버려지기 때문에 아무 값이나 입력합니다.
        pointHistoryRepository.save(new PointHistory(0, id, amount, TransactionType.CHARGE, 0));

        return userPoint;
    }

    @Override
    public UserPoint use(long id, long amount) {
        // 유효성 검사 - 사용 포인트는 0보다 커야 합니다.
        if (amount <= 0) {
            throw new ChargePointNotPositiveException();
        }

        // 로직 검사 - 사용 포인트는 보유 포인트보다 작거나 같아야 합니다.
        UserPoint userPoint = userPointRepository.selectById(id);
        if (userPoint.point() < amount) {
            throw new OutOfPointException(id, userPoint.point());
        }

        // 포인트 사용
        userPoint = userPoint.use(amount);
        userPointRepository.save(userPoint);

        // 사용 내역 저장
        // PointHistoryTable 에서는 pointHistoryId 와 updateMillis 가 버려지기 때문에 아무 값이나 입력합니다.
        pointHistoryRepository.save(new PointHistory(0, id, amount, TransactionType.USE, 0));

        return userPoint;
    }
}
