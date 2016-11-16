package ru.spbau.mit.java.tracker;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock-free integer id producer
 */
public class ThreadSafeIntIdProducer implements IdProducer<Integer>, Serializable {
    private AtomicInteger nextFreeId = new AtomicInteger();

    public ThreadSafeIntIdProducer(int start) {
        nextFreeId.set(start);
    }

    public Integer nextId() {
        while (nextFreeId.get() == -1) {
            // looping while other thread doing stuff
        }
        int next = nextFreeId.getAndSet(-1);
        if (next == Integer.MAX_VALUE) {
            // not honest, because we have one id left,
            // but I can't quickly decide how to fix it
            throw new NoFreeIdsLeftException();
        }
        nextFreeId.set(next + 1);
        return next;
    }
}
