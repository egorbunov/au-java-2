package ru.spbau.mit.java.tracker;

/**
 * Created by: Egor Gorbunov
 * Date: 11/6/16
 * Email: egor-mailbox@ya.com
 */
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
