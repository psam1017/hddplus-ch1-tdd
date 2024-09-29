package io.hhplus.tdd.point;

import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.exception.MaxPointExceededException;
import io.hhplus.tdd.point.exception.OutOfPointException;
import io.hhplus.tdd.utility.UniqueUserIdHolder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class PointTest extends TddApplicationTest {

    @DisplayName("사용자는 최대 포인트 이상으로 충전할 수 없다.")
    @Test
    void chargeUnderMax() {
        // given
        final long maxPoint = 100000;
        long chargeAmount = maxPoint + 1;
        UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());

        // when
        // then
        assertThatThrownBy(() -> userPoint.charge(chargeAmount))
                .isInstanceOf(MaxPointExceededException.class)
                .hasMessageContaining("user can have only %d points".formatted(maxPoint));
    }

    @DisplayName("사용자는 보유한 포인트를 초과하여 사용할 수 없다.")
    @Test
    void useUnderCurrentPoint() {
        // given
        UserPoint userPoint = new UserPoint(UniqueUserIdHolder.next(), 100, System.currentTimeMillis());
        long useAmount = userPoint.point() + 1;

        // when
        // then
        assertThatThrownBy(() -> userPoint.use(useAmount))
                .isInstanceOf(OutOfPointException.class)
                .hasMessageContaining("user %d has only %d points".formatted(userPoint.id(), userPoint.point()));
    }
}
