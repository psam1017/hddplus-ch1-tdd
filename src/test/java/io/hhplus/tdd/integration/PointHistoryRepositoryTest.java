package io.hhplus.tdd.integration;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointHistoryRepository;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Assertions;
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
        UserPoint userPoint = UserPoint.empty(1);
        PointHistory pointHistory = new PointHistory(0, userPoint.id(), 100, TransactionType.CHARGE, 0);

        // when
        PointHistory savedPointHistory = pointHistoryRepository.save(pointHistory);

        // then
        assertThat(savedPointHistory.id()).isGreaterThanOrEqualTo(1);
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
        UserPoint userPoint = UserPoint.empty(1);
        PointHistory pointHistory1 = new PointHistory(1, userPoint.id(), 100, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory pointHistory2 = new PointHistory(2, userPoint.id(), 50, TransactionType.USE, System.currentTimeMillis());
        pointHistoryRepository.save(pointHistory1);
        pointHistoryRepository.save(pointHistory2);

        // when
        List<PointHistory> pointHistories = pointHistoryRepository.selectAllByUserId(userPoint.id());

        // then
        assertThat(pointHistories).hasSize(2)
                .containsExactlyInAnyOrder(pointHistory1, pointHistory2);
    }
}
