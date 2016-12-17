package ru.spbau.mit.java.files.error;

public class FileNotExistsInStorage extends RuntimeException {
    private final int fileId;

    public FileNotExistsInStorage(int id) {
        this.fileId = id;
    }

    public int getFileId() {
        return fileId;
    }
}
