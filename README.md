# TDD와 동시성 제어 프로젝트 보고서

이 프로젝트는 **TDD와 동시성 제어하기**를 주제로 한 **항해플러스 6기 1주차 과제**입니다.

## 프로젝트 소개

이 과제에서 기본적으로 요구하는 것은 **테스트 코드의 작성**이었습니다. 그러나 진정한 난관은 그 다음에 있었습니다. **H2 같은 DB도 없고, `@Transactional`도 쓰지 않으면서 동시성 제어를 수행해야 하는 상황**이었습니다.

`@Transactional` 없이 프로젝트를 수행한다는 상황 자체를 머리 속으로 떠올려본 적조차 없어 당황스러웠지만, 그래도 최선을 다해 요구사항을 만족시켜보기로 했습니다.

## 문제 상황

문제는 다음의 코드 때문에 발생합니다:

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

위와 같이 **Table 역할을 하는 객체는 삽입/갱신을 할 때 항상 0.3초 이내의 지연**이 발생합니다. 추측컨데, 이렇게 함으로써 Spring Framework가 제어할 수 없는 외부 도구에 의존할 때 실제로 지연이 발생하는 상황이 재현되고 있는 것 있습니다.

이때 저희의 목표는 **동시에 요청이 들어오더라도 순서대로, 혹은 한 번에 하나의 요청씩만 제어되도록 하는 것**이었습니다.

## 기존 구조의 한계
![4 diagram-1](https://github.com/user-attachments/assets/7ecf0837-934f-4334-8fda-b8d6ed77c2d3)

일반적인 패키지 구조를 가정해봅시다:

- **Service**가 **Repository**를 호출하고,
- **Repository**가 **Table**에 데이터를 저장하는 상황입니다.

이런 상황에서 A 사용자가 포인트 **충전, 사용, 충전** 등을 번갈아가면서 요청한다면, **Table에 저장하는 지연 시간이 제각각**이라 **요청 순서대로 데이터를 제어할 수 없습니다**.

![9 file](https://github.com/user-attachments/assets/add3dfcd-9e7f-487e-be58-931103bdec6b)

빠르게 100P, 200P, 300P, ... 3,000P 를 충전하는 요청을 보냈을 때 **지연시간 때문에 요청 순서대로 데이터가 처리되지 않는 모습**을 볼 수 있습니다.

## 해결 방안

이를 해결하기 위해 두 가지 방법을 고려해보았습니다:

1. `synchronized`와 `Lock`으로 제어하기
2. **비동기 이벤트와 Queue로 제어하기**

저는 **비동기 이벤트와 Queue를 사용한 두 번째 방법**으로 데이터를 제어해보기로 했습니다. 이는 **DB의 지연 시간과 상관없이 사용자에게 빠른 응답**을 주고자 했기 때문입니다. 저장할 데이터는 빠르게 Queue 에 담아두고, 비동기 이벤트로 데이터를 제어하면 **지연 시간이 사용자에게 응답하는 데 영향을 주지 않으리라 추측**했습니다.

이 추측이 올바른지는 리드미 마지막에서 볼 수 있듯이 **k6를 사용한 성능 테스트로 확인**하였습니다.

## 1차 리팩토링: 비동기 이벤트와 Queue 적용
![5 diagram-2](https://github.com/user-attachments/assets/788d9b5a-b765-4ca1-b47f-638b7ff0efda)

제가 의도한 1차 리팩토링한 패키지 구조입니다:

- **Service**는 이제 데이터를 삽입/갱신하기 위해 **Repository**를 직접 호출하는 대신 `QueueHolder#add`를 호출하여 **Queue에 데이터를 밀어넣고**, `ApplicationEventPublisher#publishEvent`를 호출하여 **Queue에 데이터가 삽입되었음을 알리는 Event를 발행**합니다.
- 이후 **Stream 객체**는 **비동기적으로 이벤트 발생을 감지**하고 `Repository#save`를 호출하여 **Table에 데이터를 순차적으로 밀어넣습니다**.

## 트러블슈팅

시도는 좋았으나, **두 가지 문제점**이 생겼습니다:

### 1. 동시에 요청이 들어온 만큼 이벤트 비동기 함수가 같이 수행됨

동시에 요청이 들어오더라도 **각 스레드마다 비동기 함수를 실행**하려고 해서 발생하는 문제입니다. 3번의 요청이 들어오면 3번의 이벤트가 발행되어 **동시에 함수를 호출**하면 동시에 Queue에 접근해서 비우려고 합니다. 이렇게 되면 결국 **Queue를 사용하는 의미가 없어지게 될 겁니다**.

#### 해결책

이에 대한 해결방법은 비교적 간단합니다. `@Async`를 사용할 때 **수행할 스레드를 Executors로 싱글 스레드로 만들면 됩니다**.

```
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "pointEventExecutor")
    public Executor singleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
```

```
@Component
public class PointStream implements PointStream {

    @Async("pointEventExecutor")
    @EventListener(PointEvent.class)
    @Override
    public void flush() {
        // 구현 내용
    }
}
```

### 2. 최신 상태의 데이터가 Queue와 Table 어디에도 없는 상황

핵심은 두 번째 상황입니다. 아래 그림과 같이, 여기까지 만든 프로젝트에서는:

1. **Queue에서 데이터를 빼오고**,
2. **Repository에서 저장하기 위해 Table에 데이터를 전송**하는데,
3. **Table에 저장되려면 0.3초 미만의 지연이 발생**합니다.

이 **0.3초 동안 Queue에서 빠져나온 데이터는 Queue에도 없고 Table에도 없으며, 변수로 참조만 되고 있는 상태**입니다.

만약 이 상태에서 또 다른 포인트 **충전/사용 요청이 들어온다면**, 해당 요청은 **최신화된 데이터가 아니라 오래된 상태의 데이터를 참고**하기 때문에 **무결성이 깨지게 됩니다**.

![6 diagram-3](https://github.com/user-attachments/assets/c6316b26-722d-4bb8-a8ab-7e378278c6df)
![7 diagram-4](https://github.com/user-attachments/assets/e27e8ebb-0ece-4e47-a631-8974d8906967)

#### 해결책: IdentityMap의 적용

이 상황을 방지하기 위해서, 저는 **IdentityMap이라는 방법**을 사용했습니다.
![8 diagram-5](https://github.com/user-attachments/assets/30efe2f8-acca-4a62-bdc6-05fba63a6283)

**IdentityMap**은 보통 메모리에 저장한 동일한 객체가 여러 번 생성되거나 로드되는 것을 방지하는 디자인 패턴입니다. 저는 이번 프로젝트에서, **최신화된 데이터가 Table에 제대로 저장될 때까지 동일한 데이터를 메모리에 들고 있는 객체**로서 사용했습니다. 해당 IdentityMap의 최신화된 데이터는 **Table에 데이터가 안전히 저장되면(&&저장한 객체가 최신 상태와 동일하다면) 제거**하도록 합니다.

이제 **Repository는 1차적으로 IdentityMap에서 데이터를 찾고**, 여기에서 데이터를 찾지 못한 경우 **Table에서 데이터를 찾아옵니다**. 또한, **IdentityMap은 Table에 데이터가 온전히 저장될 때까지 최신 데이터를 계속 보관**하기 때문에 **Table 처럼 최신화된 상태를 보장**할 수 있습니다.

## 테스트 코드 작성

마지막으로 의도한 대로 데이터는 순차적으로, 비동기 이벤트로 제어되고, IdentityMap은 여전히 최신화된 데이터를 보관하는지를 **테스트하는 코드를 작성**하였습니다. 이후 **k6를 사용하여 현재 구조의 응답 속도에 대한 측정**도 수행했습니다.

```
void chargeAndUseByCallingOrder() {
    // given
    UserPoint userPoint = UserPoint.empty(UniqueUserIdHolder.next());
    UserPoint saveUserPoint = userPointRepository.save(userPoint);

    // when
    pointService.charge(saveUserPoint.id(), 100);
    pointService.use(saveUserPoint.id(), 100);
    pointService.charge(saveUserPoint.id(), 200);
    pointService.use(saveUserPoint.id(), 200);

    Awaitility.await()
            .atMost(2400, TimeUnit.MILLISECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .until(() -> pointService.history(saveUserPoint.id()).size() == 4);

    // then
    List<PointHistory> histories = pointService.history(saveUserPoint.id());
    assertThat(histories).hasSize(4)
            .extracting(h -> tuple(h.amount(), h.type()))
            .containsExactly(
                    tuple(100L, TransactionType.CHARGE),
                    tuple(100L, TransactionType.USE),
                    tuple(200L, TransactionType.CHARGE),
                    tuple(200L, TransactionType.USE)
            );
}
```

위 테스트는 **요청 순서대로 포인트를 조작하는지 검증하는 테스트**입니다. `PointService`는 `ApplicationEventPublisher`를 사용하여 **이벤트를 발행**하고, 이후 **비동기로 Queue의 데이터를 DB로 보내기 때문에 `PointService` 메소드의 응답은 빠르지만**, 호출 종료되어도 **데이터가 바로 Table에는 반영되지 않는다는 문제점**이 있었습니다.

따라서 **Awaitility를 사용하여 테스트를 보완**했습니다. `PointHistoryTable`의 최대 지연 시간은 0.3초이고, 총 4번 호출됩니다. 이 시간에 더하여 시스템 동작까지 고려하고 최대 소요 시간의 2배(0.3초 * 4 * 2 = 2.4초)까지 기다리기로 했습니다.

## 성능 테스트

**k6를 사용한 성능 테스트**는 아래와 같이 수행했습니다. k6의 기능을 사용하면 더 많은 걸 할 수 있지만, 이번에는 **순차적으로 데이터가 들어가는지 확인**하기 위해 아래와 같은 반복문으로 데이터를 삽입하도록 했습니다.

```
import http from 'k6/http';

export default function () {

    // 만약 순차적으로 실행하는 데 실패하여 한 번이라도 충전한 금액보다 많은 금액을 사용하게 된다면 예외가 발생한다.
    for (let i = 1; i <= 30; i++) {
        let url1 = 'http://localhost:8080/point/1/charge';
        let payload1 = `${i * 100}`;  // 100, 200, 300, ..., 3000
        let params1 = {
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
            },
        };

        let patch1 = http.patch(url1, payload1, params1);
        console.log("Status1 : " + patch1.status);

        let url2 = 'http://localhost:8080/point/1/use';
        let payload2 = `${i * 100}`;  // 100, 200, 300, ..., 3000
        let params2 = {
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
            },
        };

        let patch2 = http.patch(url2, payload2, params2);
        console.log("Status2 : " + patch2.status);
    }
}

```

위와 같이 **빠르게 60번의 요청을 보내는 자바스크립트 코드**를 실행하였습니다. 만약 비동기 처리가 없다면 지연 시간이 0~0.3초 사이이므로 **평균 0.15초의 지연**이 발생한다고 가정했을 때 총 수행 시간은 **9초(0.15초 * 60) 이상의 시간**이 걸릴 것입니다. 만약 이 시간보다 확실하게 적은 시간 내에 응답이 모두 완료된다면 **응답 속도가 개선되었다고 추측**할 수 있을 것입니다.

![12 k6-4](https://github.com/user-attachments/assets/00d7b80a-9a60-4d5b-a483-d872deb3e976)

`iteration_duration`의 결과로 확인했을 때, **60번의 요청을 하는데 총 0.316초, 평균적으로 0.005초**가 걸렸기에 **지연 없이 응답 속도가 개선되었다고 보았습니다**.

![11 k6-3](https://github.com/user-attachments/assets/67e525d5-ba61-4b38-b16d-2ebf6a3df65f)

**History 조회 결과** 역시 **순차적으로 들어가는 데 성공**했음을 확인할 수 있습니다. 참고로 데이터를 조회하는 데 **최대 0.2초 미만의 지연이 발생**함에도 불구하고, **3초(0.1초 * 30) 미만의 시간이 소요**된 것은 **Repository가 먼저 IdentityMap을 조회한 후 Table을 조회**하는데, 이때 **IdentityMap에는 지연이 없기 때문에 IdentityMap에 데이터가 존재하는 동안에는 캐시처럼 작용하여 Cache Hit을 했기 때문**입니다.

## 결론

응답 속도에 지연 없이 데이터를 처리하면서 동시에 데이터가 요청 순서대로 제어되도록 구현하기 위해 **Asynchronous**, **Event**, **Queue**를 사용했습니다. 그리고 데이터의 무결성을 보장하기 위해 **IdentityMap**으로 보완하였습니다.

그 결과, **60번의 요청에 대하여 0.3초 수준의 빠른 응답**이 가능했고, 데이터 역시 **순차적으로 처리**되었습니다.

---

## 남은 과제

주어진 시간 안에서 나름대로 생각하면서 구현했지만, 여전히 아쉬운 부분과 고민거리들은 남아있습니다.

### IdentityMap 없이 데이터의 최신 상태를 보장하기

**IdentityMap** 같은 임시 저장소가 있는 것 자체가 **오버헤드**일 수 있습니다. 저장소의 가용성을 높이기 위해 영속시킬 수 있는 도구를 도입한다면 **많은 비용**이 초래될 것입니다.  그렇다고 가벼운 메모리 저장소를 사용하는 것은 **데이터가 안전하게 영속되는 것을 보장하기 어렵습니다**. 결론적으로, **알고리즘과 구조의 개선을 통해 최신화된 데이터를 지연 없이 계속해서 보장할 수 있는 방법이 필요합니다**.

추가로, 제가 **IdentityMap**의 데이터가 **Table**의 데이터와 동일하게 최신화되었다면 삭제한다고 했었는데 회고해보니 여기에도 **무결성을 헤칠 가능성**이 있었습니다.

만약 **100P, 300P, 500P 상태**로 바뀐 **Table**에 사용자를 저장한다고 했을 때, **100P 상태의 사용자를 저장한 이후 최신화되었다고 판단해서 IdentityMap의 데이터를 삭제**하면, 결국 **300P, 500P 저장 전까지는 Table에서 100P 상태의 사용자가 조회**될 것입니다.

**이러한 문제를 극복하고자** **id 외에도 다른 상태가 같은지까지 검증**한 이후 **IdentityMap에서 삭제**했습니다. 하지만 객체의 구조가 복잡해지거나 필드가 많아지면, 그만큼 **검증하는 데 수행되는 비용**도 많이 초래됩니다. 게다가 최신화되었다고 판단하여 객체를 삭제한 그 사이에 Table의 객체가 갱신되고 또 **새로운 요청에 의해 IdentityMap에 또 상태가 다른 객체가 보관될 가능성**도 있습니다.

이 상황에서는 **UserPoint를 삭제하지 말고 계속해서 들고 있으면 해결**이 되긴 하지만, 이는 결국 **그만큼의 비용 발생**을 초래한다는 뜻이기 때문에 이 방법은, **최신화를 보장할 수는 있지만 굉장히 문제가 많은 방법**이라고 생각하게 되었습니다.

### Awaitility를 사용하지 않는 테스트

**Awaitility**는 특정 조건을 만족할 때까지 스레드를 대기시키는 도구입니다. 이 도구를 사용하는 것 자체가 **테스트에 불필요한 비용을 초래**할 수 있다는 생각이 들었습니다. 하지만 리드미를 작성하는 현재까지는 **이벤트를 받아 비동기적으로 실행되는 `PointStream`의 실행을 대기하는 방법을 찾지 못해** 일단 Awaitility를 사용했습니다. 비용이야 어찌 됐든 **일단 테스트는 통과시켜야 하고, 그래야 퇴근(중요)을 할 수 있습니다**!

즉, **Awaitility 대신 `CompletableFuture`를 사용하여 동시성 테스트를 할 수 있는 프로덕트 코드, 테스트 코드로 개선할 필요가 있습니다**. 이에 대해 피드백을 주신 코치님의 코멘트를 남기도록 하겠습니다:

> **Awaitility**
> - 팀 협업의 관점에서 Awaitility와 같은 특정한 도구를 사용하면, 도구에 대한 부담을 다른 동료들에게 줄 수 있다고 생각해요ㅎㅎ
> - 그리고 특정한 도구에 대한 결합이 생기고, 버전업이 되는 과정에서 함께 유지보수해야 하는 비용이 생길 수 있습니다ㅎㅎ
> - 따라서 Awaitility의 사용이, Awaitility가 아니면 안 되는 수준이 아니라면 저는 굳이 Awaitility를 사용하지는 않을 것 같아요ㅎㅎ
> - 현재 진행하는 과제 상황에서도 특별히 Awaitility가 아니면 안 될 부분은 없을 것 같아요!

### 잘못된 이벤트 사용

이벤트라는 개념과 구현을 정석보다는 필요에 의해 블로그를 찾아가면서 급하게 배워서 **이벤트의 올바른 사용 방법에 대해 미처 고려하지 못하고** 이벤트를 사용했습니다. 이번에 코치님의 피드백을 받으면서 **이벤트의 올바른 사용**과 관련된 **블로그 포스트**를 추천받았기에 추가로 남기도록 하겠습니다:

> **EventListener를 비동기로 처리하도록 설정할 수 있는 방법이 있습니다!**
> - 자세한 내용은 [여기](https://mangkyu.tistory.com/292)서 참고 부탁드릴게요!
>
> **이벤트는 언제 사용해야 좋은가?**
> - 이벤트는 한 도메인에서 다른 도메인으로 변경을 전파할 때 필요해요ㅎㅎ
> - 동시성을 위해 이벤트를 사용한다는 방법이 올바른 것인지 고민해볼 필요가 있을 것 같습니다!
>
> 추가적으로 작성해준 테스트 코드를 봤는데, **테스트의 속도를 많이 저하시키고 구현과 많이 강결합된 테스트라는 생각도 드네요!**
