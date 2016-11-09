package ru.spbau.mit.java.shared.request;


public class SourcesRequest {
    public static final byte code = 3;

    private final int fileId;

    public SourcesRequest(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
