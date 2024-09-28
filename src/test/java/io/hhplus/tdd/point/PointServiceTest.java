package io.hhplus.tdd.point;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumeration.TransactionType;
import io.hhplus.tdd.point.exception.MaxPointExceededException;
import io.hhplus.tdd.point.exception.OutOfPointException;
import io.hhplus.tdd.point.exception.RequestPointNotPositiveException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.utility.UniqueUserIdHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointServiceTest extends TddApplicationTest {

    @Autowired
    PointService pointService;

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @DisplayName("사용자가 현재 남은 포인트를 조회할 수 있다.")
    @Test
    void whenUserGetPoint_ThenSeeCurrentPoint() {
        // given
        UserPoint userPoint = userPointRepository.save(new UserPoint(UniqueUserIdHolder.next(), 100, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.point(userPoint.id());

        // then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(userPoint.point());
    }

    @DisplayName("사용자가 포인트 충전/이용 내역을 같이 조회할 수 있다.")
    @Test
    void whenUserGetPoint_ThenSeeAllHistories() {
        // given
        UserPoint userPoint = userPointRepository.save(UserPoint.empty(UniqueUserIdHolder.next()));
        PointHistory pointHistory1 = pointHistoryRepository.save(PointHistory.of(userPoint.id(), 100, TransactionType.CHARGE));
        PointHistory pointHistory2 = pointHistoryRepository.save(PointHistory.of(userPoint.id(), 50, TransactionType.USE));

        // when
        List<PointHistory> pointHistories = pointService.history(userPoint.id());

        // then
        assertThat(pointHistories).hasSize(2)
                .containsExactlyInAnyOrder(pointHistory1, pointHistory2);
    }

    @DisplayName("등록되지 않은 사용자는 자동으로 가입할 수 있다.")
    @Test
    void whenNotUserGetPoint_thenNoPoint() {
        // given
        long userId = UniqueUserIdHolder.next();

        // when
        UserPoint result = pointService.point(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0);
    }

    @DisplayName("사용자가 포인트를 충전할 수 있다.")
    @Test
    void userCanChargePoint() {
        // given
        UserPoint userPoint = userPointRepository.save(UserPoint.empty(UniqueUserIdHolder.next()));
        long currentPoint = userPoint.point();
        long chargeAmount = 100;

        // when
        UserPoint result = pointService.charge(userPoint.id(), chargeAmount);

        // then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(currentPoint + chargeAmount);
    }

    @DisplayName("충전 금액은 0보다 커야 한다.")
    @Test
    void whenChargeAmountLessOrEqualZero_thenCannotCharge() {
        // given
        UserPoint userPoint = userPointRepository.save(UserPoint.empty(UniqueUserIdHolder.next()));
        long chargeAmount = 0;

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userPoint.id(), chargeAmount))
                .isInstanceOf(RequestPointNotPositiveException.class)
                .hasMessage("Charge/Use point must be positive");
    }

    @DisplayName("충전 금액은 100,000보다 적어야 한다.")
    @Test
    void whenChargeAmountMoreThanMaxPoint_thenCannotCharge() {
        // given
        UserPoint userPoint = userPointRepository.save(UserPoint.empty(UniqueUserIdHolder.next()));
        long chargeAmount = 100001;

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userPoint.id(), chargeAmount))
                .isInstanceOf(MaxPointExceededException.class)
                .hasMessage("user can have only %d points".formatted(chargeAmount - 1));
    }

    @DisplayName("사용자가 포인트를 사용할 수 있다.")
    @Test
    void userCanUsePoint() {
        // given
        long point = 100;
        long useAmount = 100;
        UserPoint userPoint = userPointRepository.save(new UserPoint(UniqueUserIdHolder.next(), point, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.use(userPoint.id(), useAmount);

        // then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isZero();
    }

    @DisplayName("사용할 포인트는 0원보다 커야 한다.")
    @Test
    void whenUseAmountLessOrEqualZero_thenCannotUse() {
        // given
        UserPoint userPoint = userPointRepository.save(new UserPoint(UniqueUserIdHolder.next(), 100, System.currentTimeMillis()));
        long useAmount = 0;

        // when
        // then
        assertThatThrownBy(() -> pointService.use(userPoint.id(), useAmount))
                .isInstanceOf(RequestPointNotPositiveException.class)
                .hasMessage("Charge/Use point must be positive");
    }

    @DisplayName("잔고가 부족하면 포인트를 사용할 수 없다.")
    @Test
    void whenPointNotEnough_ThenCannotUse() {
        // given
        UserPoint userPoint = userPointRepository.save(new UserPoint(UniqueUserIdHolder.next(), 10, System.currentTimeMillis()));
        long useAmount = userPoint.point() + 1;

        // when
        // then
        assertThatThrownBy(() -> pointService.use(userPoint.id(), useAmount))
                .isInstanceOf(OutOfPointException.class)
                .hasMessage("user %d has only %d points".formatted(userPoint.id(), userPoint.point()));
    }

    @DisplayName("사용자가 요청한 순서대로 포인트를 조작할 수 있다.")
    @Test
    void chargeInConcurrency() throws InterruptedException {
        // given
        UserPoint userPoint = userPointRepository.save(UserPoint.empty(UniqueUserIdHolder.next()));

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);
        List<Runnable> tasks = List.of(
                () -> pointService.charge(userPoint.id(), 100),
                () -> pointService.charge(userPoint.id(), 200),
                () -> pointService.charge(userPoint.id(), 300),
                () -> pointService.charge(userPoint.id(), 400)
        );

        // when
        tasks.forEach(task -> executorService.submit(() -> {
            try {
                task.run();
            } finally {
                latch.countDown();
            }
        }));
        latch.await();

        // then
        UserPoint userPoint2 = pointService.point(userPoint.id());
        assertThat(userPoint2.point()).isEqualTo(1000);
    }
}
