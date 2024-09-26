package io.hhplus.tdd.point;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PointHistoryQueueHolder {

    /*
    LinkedBlockingQueue
    - producer 가 많고, consumer 가 하나일 때 사용하기 좋다.
    - 필요한 경우 Queue Size 를 지정할 수 있다.
     */
    private static final Queue<PointHistory> userPointQueue = new LinkedBlockingQueue<>();

    public static void add(PointHistory userPoint) {
        userPointQueue.add(userPoint);
    }

    public static boolean isEmpty() {
        return userPointQueue.isEmpty();
    }

    public static PointHistory poll() {
        return userPointQueue.poll();
    }
}
