package io.hhplus.tdd.integration;

import io.hhplus.tdd.infrastructure.UniqueUserIdHolder;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

// 테스트 과정에 지연이 발생하지 않도록 비활성화시킵니다.
public class PointStreamServiceTest extends TddApplicationIntegrationTest {

    @Autowired
    PointStream pointStream;

    @Autowired
    PointService pointService;

    @Autowired
    UserPointRepository userPointRepository;

    @DisplayName("PointStream 이 이벤트 발생을 감지할 수 있다.")
    @Test
    void chargeByCallingOrder() throws InterruptedException, ExecutionException {
        // given
        UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());
        UserPoint saveUserPoint = userPointRepository.save(userPoint);
        CompletableFuture<Void> charge1 = CompletableFuture.runAsync(() -> pointService.charge(saveUserPoint.id(), 100));
        CompletableFuture<Void> use1 = CompletableFuture.runAsync(() -> pointService.use(saveUserPoint.id(), 100));
        CompletableFuture<Void> charge2 = CompletableFuture.runAsync(() -> pointService.charge(saveUserPoint.id(), 200));
        CompletableFuture<Void> use2 = CompletableFuture.runAsync(() -> pointService.use(saveUserPoint.id(), 200));

        // when
        CompletableFuture
                .allOf(charge1, use1, charge2, use2)
                .join();

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

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
