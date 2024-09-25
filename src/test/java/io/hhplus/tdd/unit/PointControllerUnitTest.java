package io.hhplus.tdd.unit;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.enumeration.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PointControllerUnitTest extends TddApplicationUnitTest {

    /*
     * 테스트 작성 이유 : 사용자가 포인트를 성공적으로 조회하는 것을 확인합니다.
     */
    @DisplayName("사용자가 현재 남은 포인트를 조회할 수 있다.")
    @Test
    void whenUserGetPoint_ThenSeeCurrentPoint() {
        // given
        UserPoint userPoint = BEAN_CONTAINER.userPointRepository.save(new UserPoint(1, 100, System.currentTimeMillis()));

        // when
        UserPoint result = BEAN_CONTAINER.pointController.point(userPoint.id());

        // then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(userPoint.point());
    }

    /*
     * 테스트 작성 이유 : 사용자는 충전한 내역과 이용한 내역 모두를 조회할 수 있어야 합니다.
     */
    @DisplayName("사용자가 포인트 충전/이용 내역을 모두 조회할 수 있다.")
    @Test
    void whenUserGetPoint_ThenSeeAllHistories() {
        // given
        UserPoint userPoint = BEAN_CONTAINER.userPointRepository.save(new UserPoint(1, 100, System.currentTimeMillis()));
        PointHistory pointHistory1 = BEAN_CONTAINER.pointHistoryRepository.save(new PointHistory(1, userPoint.id(), 100, TransactionType.CHARGE, System.currentTimeMillis()));
        PointHistory pointHistory2 = BEAN_CONTAINER.pointHistoryRepository.save(new PointHistory(2, userPoint.id(), 50, TransactionType.USE, System.currentTimeMillis()));

        // when
        List<PointHistory> pointHistories = BEAN_CONTAINER.pointController.history(1);

        // then
        assertThat(pointHistories).hasSize(2)
                .containsExactlyInAnyOrder(pointHistory1, pointHistory2);
    }

    /*
     * 테스트 작성 이유 : 사용자는 포인트를 충전할 수 있어야 합니다.
     */
    @DisplayName("사용자가 포인트를 충전할 수 있다.")
    @Test
    void userCanChargePoint() {
        // given
        UserPoint userPoint = BEAN_CONTAINER.userPointRepository.save(new UserPoint(1, 100, System.currentTimeMillis()));
        long currentPoint = userPoint.point();
        long chargeAmount = 100;

        // when
        UserPoint result = BEAN_CONTAINER.pointController.charge(userPoint.id(), chargeAmount);

        // then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(currentPoint + chargeAmount);
    }

    /*
     * 테스트 작성 이유 : 사용자는 포인트를 사용할 수 있어야 합니다. 이때 사용자는 제약 없이 남은 포인트를 100% 모두 소진할 수 있습니다.(경계값 분석)
     */
    @DisplayName("사용자가 포인트를 사용할 수 있다.")
    @Test
    void userCanUsePoint() {
        // given
        long point = 100;
        long useAmount = 100;
        UserPoint userPoint = BEAN_CONTAINER.userPointRepository.save(new UserPoint(1, point, System.currentTimeMillis()));

        // when
        UserPoint result = BEAN_CONTAINER.pointController.use(userPoint.id(), useAmount);

        // then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isZero();
    }
}
