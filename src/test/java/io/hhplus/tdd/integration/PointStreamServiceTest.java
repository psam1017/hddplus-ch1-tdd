package io.hhplus.tdd.integration;

import io.hhplus.tdd.infrastructure.UniqueUserIdHolder;
import io.hhplus.tdd.point.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class PointStreamServiceTest extends TddApplicationIntegrationTest {

    @Autowired
    PointStream pointStream;

    @Autowired
    PointService pointService;

    @Autowired
    UserPointRepository userPointRepository;

    @DisplayName("사용자가 요청한 순서대로 포인트를 조작할 수 있다.")
    @Test
    void chargeByCallingOrder() {
        // given
        UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());
        UserPoint saveUserPoint = userPointRepository.save(userPoint);

        pointService.charge(saveUserPoint.id(), 100);
        pointService.use(saveUserPoint.id(), 100);
        pointService.charge(saveUserPoint.id(), 200);
        pointService.use(saveUserPoint.id(), 200);

        // PointHistoryTable 의 최대 지연 시간은 0.3 초 입니다.
        // System 동작 시간까지 고려해서 1.2 * 2 = 2.4 초 이상으로 설정합니다.
        // when
        Awaitility.await()
                .atMost(2400, TimeUnit.MILLISECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> pointService.history(saveUserPoint.id()).size() == 4);

        // then
        List<PointHistory> histories = pointService.history(saveUserPoint.id());
        assertThat(histories).hasSize(4)
                .extracting(h -> tuple(h.amount(), h.type()))
                .containsExactly(
                        tuple(100L, TransactionType.CHARGE),
                        tuple(100L, TransactionType.USE),
                        tuple(200L, TransactionType.CHARGE),
                        tuple(200L, TransactionType.USE)
                );
    }
}
