package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumeration.TransactionType;
import io.hhplus.tdd.point.exception.RequestPointNotPositiveException;
import io.hhplus.tdd.point.exception.MaxPointExceededException;
import io.hhplus.tdd.point.exception.OutOfPointException;
import io.hhplus.tdd.point.infrastructure.PointLockHolder;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointLockHolder pointLockHolder;

    public PointService(UserPointRepository userPointRepository, PointHistoryRepository pointHistoryRepository, PointLockHolder pointLockHolder) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.pointLockHolder = pointLockHolder;
    }

    public UserPoint point(long id) {
        return userPointRepository.selectById(id);
    }

    public List<PointHistory> history(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    public UserPoint charge(long id, long amount) {
        pointLockHolder.lock(id);
        try {
            if (amount <= 0) {
                throw new RequestPointNotPositiveException();
            }
            UserPoint userPoint = userPointRepository.selectById(id).charge(amount);
            userPoint = userPointRepository.save(userPoint);
            pointHistoryRepository.save(PointHistory.of(id, amount, TransactionType.CHARGE));
            return userPoint;
        } finally {
            pointLockHolder.unlock(id);
        }
    }

    public UserPoint use(long id, long amount) {
        pointLockHolder.lock(id);
        try {
            if (amount <= 0) {
                throw new RequestPointNotPositiveException();
            }
            UserPoint userPoint = userPointRepository.selectById(id).use(amount);
            userPoint = userPointRepository.save(userPoint);
            pointHistoryRepository.save(PointHistory.of(id, amount, TransactionType.USE));
            return userPoint;
        } finally {
            pointLockHolder.unlock(id);
        }
    }
}
