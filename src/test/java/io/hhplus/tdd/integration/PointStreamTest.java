package io.hhplus.tdd.integration;

import io.hhplus.tdd.infrastructure.UniqueUserIdHolder;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumeration.TransactionType;
import io.hhplus.tdd.point.infrastructure.PointEvent;
import io.hhplus.tdd.point.infrastructure.PointHistoryQueueHolder;
import io.hhplus.tdd.point.infrastructure.UserPointIdentityMap;
import io.hhplus.tdd.point.infrastructure.UserPointQueueHolder;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.stream.PointStream;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@Disabled
public class PointStreamTest extends TddApplicationIntegrationTest {

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    PointStream pointStream;

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    /*
     * 테스트 작성 이유 : PointStream 이 PointEvent 발행을 감지하여 QueueHolder 에서 UserPoint, PointHistory 를 꺼내어 DB 에 성공적으로 저장함을 검증하는 테스트입니다.
     * Awaitility 를 사용하여 비동기로 동작하는 PointStream 의 flush() 메서드가 실행되는 것을 확인합니다.
     * BDDMockito#verify() 를 사용하여 PointStream, UserPointRepository, PointHistoryRepository 의 save() 메서드가 실제로 각각 1회씩 호출되었음을 검증합니다.
     * 각 Table 의 Command 최대 지연 시간은 0.3초입니다. 시스템 동작 시간까지 고려해서 0.3 * 2 = 0.6 초 이상으로 설정합니다.
     */
    @DisplayName("PointStream 이 PointEvent 발행을 감지할 수 있다.")
    @Test
    void listenPointEvent() {
        // given
        UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());
        UserPointQueueHolder.add(userPoint);
        PointHistory pointHistory = new PointHistory(0, userPoint.id(), 100, TransactionType.CHARGE, 0);
        PointHistoryQueueHolder.add(pointHistory);

        // when
        applicationEventPublisher.publishEvent(new PointEvent(this));

        Awaitility.await()
                .atMost(1200, TimeUnit.MILLISECONDS)
                .until(() -> UserPointIdentityMap.find(userPoint.id()).isEmpty());

        // then
        assertThat(UserPointIdentityMap.find(userPoint.id()).isEmpty()).isTrue();
        assertThat(UserPointQueueHolder.isEmpty()).isTrue();
        assertThat(PointHistoryQueueHolder.isEmpty()).isTrue();

        verify(pointStream, times(1)).flush();
        verify(userPointRepository, times(1)).save(userPoint);
        verify(pointHistoryRepository, times(1)).save(pointHistory);
    }
}
