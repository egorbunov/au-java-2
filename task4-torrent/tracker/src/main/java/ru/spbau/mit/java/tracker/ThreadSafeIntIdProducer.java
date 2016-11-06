package ru.spbau.mit.java.tracker;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeIntIdProducer implements IdProducer<Integer> {
    AtomicInteger nextFreeId;

    public ThreadSafeIntIdProducer(int start) {
        nextFreeId.set(start);
    }

    public Integer nextId() {
        return nextFreeId.getAndIncrement();
    }
}
