package ru.spbau.mit.java.tracker;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeIntIdProducer implements IdProducer<Integer>, Serializable {
    private AtomicInteger nextFreeId = new AtomicInteger();

    public ThreadSafeIntIdProducer(int start) {
        nextFreeId.set(start);
    }

    public Integer nextId() {
        return nextFreeId.getAndIncrement();
    }
}
