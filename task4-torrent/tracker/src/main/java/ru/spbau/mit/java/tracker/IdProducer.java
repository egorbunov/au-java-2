package ru.spbau.mit.java.tracker;

public interface IdProducer<T> {
    T nextId();
}
