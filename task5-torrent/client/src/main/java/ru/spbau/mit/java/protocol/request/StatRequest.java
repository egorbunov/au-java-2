package ru.spbau.mit.java.protocol.request;

public class StatRequest {
    public static final byte code = 1;

    private final int fileId;

    public StatRequest(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
