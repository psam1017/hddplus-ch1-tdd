package io.hhplus.tdd.point;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class UserPointQueueHolder {

    /*
    LinkedBlockingQueue
    - producer 가 많고, consumer 가 하나일 때 사용하기 좋다.
    - 필요한 경우 Queue Size 를 지정할 수 있다.
     */
    private static final Queue<UserPoint> userPointQueue = new LinkedBlockingQueue<>();

    public static void add(UserPoint userPoint) {
        userPointQueue.add(userPoint);
    }

    public static boolean isEmpty() {
        return userPointQueue.isEmpty();
    }

    public static UserPoint poll() {
        return userPointQueue.poll();
    }

    public static Optional<UserPoint> search(long userId) {
        return userPointQueue.stream()
                .filter(userPoint -> userPoint.id() == userId)
                .findFirst();
    }
}
