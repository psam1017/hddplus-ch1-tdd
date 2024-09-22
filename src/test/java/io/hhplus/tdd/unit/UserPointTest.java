package io.hhplus.tdd.unit;

import io.hhplus.tdd.point.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class UserPointTest {

    @DisplayName("사용자가 포인트를 충전할 수 있다.")
    @Test
    void userCanChargePoint() {
        // given
        UserPoint userPoint = UserPoint.empty(1);
        long currentPoint = userPoint.point();
        long chargeAmount = 100;

        // when
        UserPoint chargedUserPoint = userPoint.charge(chargeAmount);

        // then
        assertThat(chargedUserPoint.id()).isEqualTo(userPoint.id());
        assertThat(chargedUserPoint.point()).isEqualTo(currentPoint + chargeAmount);
    }

    @DisplayName("사용자가 포인트를 사용할 수 있다.")
    @Test
    void userCanUsePoint() {
        // given
        UserPoint userPoint = new UserPoint(1, 100, System.currentTimeMillis());
        long currentPoint = userPoint.point();
        long useAmount = 100;

        // when
        UserPoint usedUserPoint = userPoint.use(useAmount);

        // then
        assertThat(usedUserPoint.id()).isEqualTo(userPoint.id());
        assertThat(usedUserPoint.point()).isEqualTo(currentPoint - useAmount);
    }
}
