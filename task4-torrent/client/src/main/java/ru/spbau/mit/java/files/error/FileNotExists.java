package ru.spbau.mit.java.files.error;

public class FileNotExists extends RuntimeException {
    private final int fileId;

    public FileNotExists(int id) {
        this.fileId = id;
    }

    public int getFileId() {
        return fileId;
    }
}
