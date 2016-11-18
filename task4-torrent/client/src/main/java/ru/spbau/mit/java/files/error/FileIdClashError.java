package ru.spbau.mit.java.files.error;


/**
 * Throw in case file with already existing is added to the storage
 */
public class FileIdClashError extends RuntimeException {
    private final int fileId;

    public FileIdClashError(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
