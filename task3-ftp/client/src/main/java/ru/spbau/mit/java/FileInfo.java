package ru.spbau.mit.java;

/**
 *
 */
public class FileInfo {
    private final String name;
    private final boolean isDirectory;

    public FileInfo(String name, boolean isDirectory) {
        this.isDirectory = isDirectory;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
