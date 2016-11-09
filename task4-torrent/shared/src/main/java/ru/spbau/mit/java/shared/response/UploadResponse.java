package ru.spbau.mit.java.shared.response;


public class UploadResponse {
    private final int fileId;


    public UploadResponse(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
