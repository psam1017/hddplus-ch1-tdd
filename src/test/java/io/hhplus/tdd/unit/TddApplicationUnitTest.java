package io.hhplus.tdd.unit;

import io.hhplus.tdd.mock.BeanContainer;

public abstract class TddApplicationUnitTest {

    protected static final BeanContainer BEAN_CONTAINER = new BeanContainer();

    // TODO: 2024-09-22 만약 도메인 메서드가 생긴다면 단위 테스트
    // TODO: 2024-09-21 포인트 정책 추가. 잔고부족 및 최대잔고 등
    // TODO: 2024-09-21 integration package 에서 동시성 제어를 하기 위한 테스트 케이스 작성 후 동시성 제어 기능 구현
    // TODO: 2024-09-21 동시성 제어를 하고 나면, 동일한 순서가 보장되어야 한다 -> 톻합 테스트에서 테스트
}
