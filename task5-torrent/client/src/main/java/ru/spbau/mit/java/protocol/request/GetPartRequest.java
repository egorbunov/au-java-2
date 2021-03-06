package ru.spbau.mit.java.protocol.request;


public class GetPartRequest {
    public static final byte code = 2;

    private final int fileId;
    private final int partId;

    public GetPartRequest(int fileId, int partId) {
        this.fileId = fileId;
        this.partId = partId;
    }

    public int getFileId() {
        return fileId;
    }

    public int getPartId() {
        return partId;
    }

    @Override
    public String toString() {
        return "{file_id: " + fileId + ", part_id: " + partId + "}";
    }
}
