package io.hhplus.tdd.integration;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.enumeration.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.infrastructure.UniqueUserIdHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PointHistoryRepositoryTest extends TddApplicationIntegrationTest {

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    /*
     * 테스트 작성 이유 : 포인트 이력을 저장할 수 있어야 합니다.
     */
    @DisplayName("포인트 이력을 저장할 수 있다.")
    @Test
    void save() {
        // given
        UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());
        PointHistory pointHistory = new PointHistory(0, userPoint.id(), 100, TransactionType.CHARGE, 0);

        // when
        PointHistory savedPointHistory = pointHistoryRepository.save(pointHistory);

        // then
        assertThat(savedPointHistory.id()).isGreaterThan(0);
        assertThat(savedPointHistory.userId()).isEqualTo(userPoint.id());
        assertThat(savedPointHistory.amount()).isEqualTo(pointHistory.amount());
        assertThat(savedPointHistory.type()).isEqualTo(pointHistory.type());
        assertThat(savedPointHistory.updateMillis()).isEqualTo(pointHistory.updateMillis());
    }

    /*
     * 테스트 작성 이유 : 사용자의 모든 포인트 이력을 조회할 수 있어야 합니다.
     */
    @DisplayName("사용자의 모든 포인트 이력을 조회할 수 있다.")
    @Test
    void selectAllByUserId() {
        // given
        UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());
        PointHistory pointHistory1 = pointHistoryRepository.save(new PointHistory(0, userPoint.id(), 100, TransactionType.CHARGE, 0));
        PointHistory pointHistory2 = pointHistoryRepository.save(new PointHistory(0, userPoint.id(), 50, TransactionType.USE, 0));

        // when
        List<PointHistory> pointHistories = pointHistoryRepository.selectAllByUserId(userPoint.id());

        // then
        assertThat(pointHistories).hasSize(2)
                .containsExactlyInAnyOrder(pointHistory1, pointHistory2);
    }
}
