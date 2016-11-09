package ru.spbau.mit.java.files;

import java.io.File;

public class FileNotExists extends RuntimeException {
    private final int fileId;

    public FileNotExists(int id) {
        this.fileId = id;
    }

    public int getFileId() {
        return fileId;
    }
}
