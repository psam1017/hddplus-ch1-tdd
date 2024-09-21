package io.hhplus.tdd.unit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserPointRepositoryUnitTest extends TddApplicationUnitTest {

    /*
     * PointControllerTest 를 만드는 과정에서 UserPoint 를 영속시킬 책임을 지닌 객체가 필요했습니다.
     * UserPointRepository 라는 인터페이스를 만들었고, 이 인터페이스가 제대로 동작하는지를 테스트합니다.
     * 기본적으로 영속시키는 것 자체는 디스크에 대한 쓰기 작업이며 비즈니스 로직을 담지 않기에 테스트 대상이 아닙니다.
     * 하지만, 특수하게 UserPointTable 에서 selectById 에 의해 예외나 Optional.empty 가 반환되지 않기에 등록되지 않은 사용자더라도 예외가 발생하지 않는 프로세스로 이해했습니다.
     * 이에 등록되지 않은 사용자더라도 예외가 발생하지 않는 동작을 테스트합니다.
     */
    @DisplayName("사용자를 등록할 수 있다.")
    @Test
    void whenFindNotRegisteredUser_ThenNoException() {
        // given
        long notExistUserId = 999L;

        // when
        // then
        Assertions.assertThatCode(() -> getBeanContainer().userPointRepository.selectById(notExistUserId))
                .doesNotThrowAnyException();
    }
}
