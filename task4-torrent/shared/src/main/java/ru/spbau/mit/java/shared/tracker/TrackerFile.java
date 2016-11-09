package ru.spbau.mit.java.shared.tracker;

public class TrackerFile<T> {
    private final T id;
    private final String name;
    private final int size;

    public TrackerFile(T id, String name, int size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public T getId() {
        return id;
    }
}
