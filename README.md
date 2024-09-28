# TDD와 동시성 제어 프로젝트 보고서

이 프로젝트는 **TDD와 동시성 제어하기**를 주제로 한 **항해플러스 6기 1주차 과제**입니다.

## 프로젝트 소개

이 과제에서 기본적으로 요구하는 것은 **테스트 코드의 작성**이었습니다. 그러나 진정한 난관은 그 다음에 있었습니다. **H2 같은 DB도 없고, `@Transactional`도 쓰지 않으면서 동시성 제어를 수행해야 하는 상황**이었죠.

`@Transactional` 없이 프로젝트를 수행한다는 상황 자체를 머리 속으로 떠올려본 적조차 없어 당황스러웠지만, 그래도 최선을 다해 요구사항을 만족시키기 위해 노력했습니다.

## 문제 상황

다음과 같은 코드에서 문제가 발생했습니다:

```
public PointHistory insert(long userId, long amount, TransactionType type, long updateMillis) {
    throttle(300L);
    PointHistory pointHistory = new PointHistory(cursor++, userId, amount, type, updateMillis);
    table.add(pointHistory);
    return pointHistory;
}

private void throttle(long millis) {
    try {
        TimeUnit.MILLISECONDS.sleep((long) (Math.random() * millis));
    } catch (InterruptedException ignored) {
    }
}
```

위 코드에서 **Table 역할을 하는 객체는 삽입/갱신 시 항상 0.3초 이내의 지연이 발생**합니다. 이는 Spring Framework가 제어할 수 없는 외부 도구에 의존하는 상황을 재현하는 것 같습니다.

이러한 지연으로 인해 **동시에 여러 요청이 들어와도 요청 순서가 보장되지 않는 문제**가 발생하였습니다. **동시성 제어**를 통해 **동시에 들어오는 요청을 순서대로 처리**하거나, **한 번에 하나의 요청만 처리**하도록 해야 했습니다.

## 기존 구조의 한계

![4 diagram-1](https://github.com/user-attachments/assets/7ecf0837-934f-4334-8fda-b8d6ed77c2d3)

일반적인 구조에서 **Service**는 **Repository**를 호출하고, **Repository**는 **Table**에 데이터를 저장합니다. 이때 **다수의 요청**이 동시에 들어올 경우 **Table에 저장되는 데이터의 순서**가 보장되지 않는 문제가 발생합니다.

특히, 같은 사용자가 포인트를 **충전**하거나 **사용**할 때, **요청 순서**가 중요한데, 지연 시간 때문에 요청이 순서대로 처리되지 않는 경우가 발생했습니다.

## 해결 방안

이를 해결하기 위해 두 가지 방법을 고려했습니다:

1. **`synchronized`와 `Lock`으로 동시성 제어**
2. **비동기 이벤트와 Queue로 동시성 제어**

처음에는 비동기 이벤트와 Queue를 사용하려 했으나, 코치님의 피드백을 바탕으로 최종적으로 **Lock**을 이용한 동시성 제어를 선택했습니다. 이를 통해 **동시에 여러 요청이 들어와도 같은 사용자에 대한 요청은 순서대로 처리**되도록 구현했습니다.

### Lock을 사용한 동시성 제어

```
public UserPoint charge(long id, long amount) {
    pointLockHolder.lock(id);
    try {
        if (amount <= 0) {
            throw new RequestPointNotPositiveException();
        }
        UserPoint userPoint = userPointRepository.selectById(id).charge(amount);
        userPoint = userPointRepository.save(userPoint);
        pointHistoryRepository.save(PointHistory.of(id, amount, TransactionType.CHARGE));
        return userPoint;
    } finally {
        pointLockHolder.unlock(id);
    }
}
```

```
public class PointLockHolder {

    private final Map<Long, Lock> locks = new ConcurrentHashMap<>();

    public void lock(long id) {
        getLock(id).lock();
    }

    public void unlock(long id) {
        getLock(id).unlock();
    }

    private Lock getLock(long id) {
        return locks.computeIfAbsent(id, k -> new ReentrantLock());
    }
}
```

**PointLockHolder** 클래스를 사용하여 **사용자 ID 별로 Lock을 생성**하고, 각 사용자의 요청에 대해 **순차적으로 처리**할 수 있도록 동시성을 제어했습니다. 이를 통해 **같은 사용자의 요청은 동시에 처리되지 않도록** 하면서도 **다른 사용자들 간의 요청은 동시에 처리**할 수 있게 되었습니다.

## 테스트 코드

테스트 코드를 통해 **동시성 제어**가 제대로 이루어지는지 검증했습니다. 다음은 간단한 테스트 코드의 예시입니다.

```
@DisplayName("사용자가 요청한 순서대로 포인트를 조작할 수 있다.")
@Test
void chargeInConcurrency() throws InterruptedException {
    // given
    UserPoint userPoint = userPointRepository.save(UserPoint.empty(UniqueUserIdHolder.next()));

    ExecutorService executorService = Executors.newFixedThreadPool(4);
    CountDownLatch latch = new CountDownLatch(4);
    List<Runnable> tasks = List.of(
            () -> pointService.charge(userPoint.id(), 100),
            () -> pointService.charge(userPoint.id(), 200),
            () -> pointService.charge(userPoint.id(), 300),
            () -> pointService.charge(userPoint.id(), 400)
    );

    // when
    tasks.forEach(task -> executorService.submit(() -> {
        try {
            task.run();
        } finally {
            latch.countDown();
        }
    }));
    latch.await();

    // then
    UserPoint userPoint2 = pointService.point(userPoint.id());
    assertThat(userPoint2.point()).isEqualTo(1000);
}
}
```

이 테스트 코드를 통해 **충전**과 **사용** 요청이 올바르게 순서대로 처리되고, **동시성 문제 없이 기록**되는지 확인했습니다.

## 결론

- **ReentrantLock**를 통해 동시성 제어를 수행하고 같은 사용자의 **다중 요청**을 순서대로 처리할 수 있게 되었습니다.
- 각 **사용자마다 다른 Lock**을 부여하여 **다른 사용자들 간에는 요청이 동시에 처리**되어, **효율성**을 유지했습니다.
- 테스트 코드를 통해 실제로 동시에 요청이 들어오더라도 **데이터 일관성**이 보장되는 것을 검증했습니다.
