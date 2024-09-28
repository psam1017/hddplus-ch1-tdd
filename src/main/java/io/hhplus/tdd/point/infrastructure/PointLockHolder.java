package io.hhplus.tdd.point.infrastructure;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
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
