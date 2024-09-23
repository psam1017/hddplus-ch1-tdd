package io.hhplus.tdd.integration;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.UserPointRepository;
import io.hhplus.tdd.infrastructure.UniqueUserIdHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class UserPointRepositoryTest extends TddApplicationIntegrationTest {
    
    @Autowired
    UserPointRepository userPointRepository;

    /*
     * 테스트 작성 이유 : 사용자가 포인트를 적립할 수 있어야 합니다.
     */
    @DisplayName("사용자가 포인트를 적립할 수 있다.")
    @Test
    void save() {
        // given
        int point = 100;
        UserPoint userPoint = new UserPoint(UniqueUserIdHolder.next(), point, System.currentTimeMillis());
        
        // when
        UserPoint savedUserPoint = userPointRepository.save(userPoint);
        
        // then
        assertThat(savedUserPoint.id()).isEqualTo(userPoint.id());
        assertThat(savedUserPoint.point()).isEqualTo(userPoint.point());
    }

    /*
     * 테스트 작성 이유 : 사용자가 포인트를 조회할 수 있어야 합니다.
     */
    @DisplayName("사용자가 포인트를 조회할 수 있다.")
    @Test
    void selectById() {
        // given
        UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());
        userPointRepository.save(userPoint);

        // when
        UserPoint selectedUserPoint = userPointRepository.selectById(userPoint.id());

        // then
        assertThat(selectedUserPoint.id()).isEqualTo(userPoint.id());
        assertThat(selectedUserPoint.point()).isEqualTo(userPoint.point());
    }
}
