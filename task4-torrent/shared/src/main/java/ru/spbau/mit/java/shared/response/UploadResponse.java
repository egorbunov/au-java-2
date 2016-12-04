package ru.spbau.mit.java.shared.response;


public class UploadResponse {
    private final int fileId;


    public UploadResponse(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }

    @Override
    public String toString() {
        return "{file_id: " + Integer.toString(fileId) + "}";
    }
}
