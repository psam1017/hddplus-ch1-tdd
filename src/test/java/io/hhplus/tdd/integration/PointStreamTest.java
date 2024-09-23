package io.hhplus.tdd.integration;

import io.hhplus.tdd.infrastructure.UniqueUserIdHolder;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class PointStreamTest extends TddApplicationIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointStreamTest.class);

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    PointService pointService;

    /*
     * 테스트 작성 이유 : 지연 시간과 상관 없이 순차적으로 포인트를 충전할 수 있도록 합니다.
     * 이를 위해 첫 번째로 호출하면 넉넉하게 1+초 이상의 지연이 발생하는 Decorator 로 테스트를 수행합니다.
     * Decorator 패턴을 사용하기 위해 PointService 의 메소드를 호출합니다.
     * PointHistoryTable, UserPointTable 은 0.3 초보다 적은 지연이 발생하기 때문에 실행 순서를 확인할 수 있습니다.
     */
    @DisplayName("지연 여부와 상관 없이 호출 순서로 포인트를 충전할 수 있다.")
    @Test
    void chargeByCallingOrder() throws InterruptedException {
        // given
        UserPoint userPoint = new UserPoint(UniqueUserIdHolder.next(), 0, System.currentTimeMillis());
        UserPoint saveUserPoint = userPointRepository.save(userPoint);
        Thread t1 = new Thread(() -> pointService.charge(saveUserPoint.id(), 100));
        Thread t2 = new Thread(() -> pointService.charge(saveUserPoint.id(), 200));

        t1.start(); // 소요시간 : 1+초 지연 + 포인트 저장 0.2초 + 내역 저장 0.3초 = 1.5+초 이상
        sleep(1000); // 첫 번째 우선 스레드 실행 보장
        t2.start(); // 소요시간 : 포인트 저장 0.2초 + 내역 저장 0.3초 = 0.5초 이상

        t1.join();
        t2.join();

        // when
        List<PointHistory> histories = pointService.history(saveUserPoint.id());

        // then
        assertThat(histories).hasSize(2)
                .extracting(PointHistory::amount)
                .containsExactly(
                        100L,
                        200L
                );
    }

    @TestConfiguration
    static class PointStreamTestConfig {

        @Autowired
        PointHistoryRepository pointHistoryRepository;

        @Primary
        @Bean
        public PointHistoryRepository pointService() {
            return new PointHistoryRepositoryDecorator(pointHistoryRepository);
        }
    }

    static class PointHistoryRepositoryDecorator implements PointHistoryRepository {

        private final PointHistoryRepository delegate;
        private final AtomicBoolean isSaveMethodFirstCalled = new AtomicBoolean(true);

        public PointHistoryRepositoryDecorator(PointHistoryRepository delegate) {
            this.delegate = delegate;
        }

        /*
         * 첫 번째 호출이면 1+초 지연을 발생시킵니다.
         */
        @Override
        public PointHistory save(PointHistory pointHistory) {
            if (isSaveMethodFirstCalled.getAndSet(false)) {
                log.info("first call waits 2 seconds");
                sleep(2000);
                log.info("first call starts");
                return delegate.save(pointHistory);
            }
            log.info("second call starts");
            return delegate.save(pointHistory);
        }

        @Override
        public List<PointHistory> selectAllByUserId(long userId) {
            return delegate.selectAllByUserId(userId);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
