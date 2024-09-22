package io.hhplus.tdd.unit;

import io.hhplus.tdd.mock.BeanContainer;

/**
 * Repository 는 단위 테스트를 하지 않습니다.
 * 1. Repository 는 애플리케이션 기본적으로 외부 시스템을 사용하기 때문에 단위 테스트가 불가능합니다. 즉, 무조건 통합테스트를 수행해야 합니다.
 * 2. Repository 는 Service 가 호출할 Infrastructure Component 입니다. 즉, Repository 테스크 커버리지가 낮거나 하다면 이는 불필요한 메서드가 남아있거나, Service 가 충분히 테스트되지 않음을 의미합니다.
 * 3. 2번에 의해, 통합테스트에서도 Repository 는 따로 테스트하지 않습니다.
 */
public abstract class TddApplicationUnitTest {

    protected static final BeanContainer BEAN_CONTAINER = new BeanContainer();

    // TODO: 2024-09-22 만약 도메인 메서드가 생긴다면 단위 테스트
    // TODO: 2024-09-22 통합 테스트 진행
    // TODO: 2024-09-21 포인트 정책 추가. 잔고부족 및 최대잔고 등
    // TODO: 2024-09-21 integration package 에서 동시성 제어를 하기 위한 테스트 케이스 작성 후 동시성 제어 기능 구현
    // TODO: 2024-09-21 동시성 제어를 하고 나면, 동일한 순서가 보장되어야 한다 -> 톻합 테스트에서 테스트
}
