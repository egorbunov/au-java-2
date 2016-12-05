package ru.spbau.mit.java.shared.tracker;


public class FileInfo {
    private final int size;
    private final String name;

    public FileInfo(int size, String name) {
        this.size = size;
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}
