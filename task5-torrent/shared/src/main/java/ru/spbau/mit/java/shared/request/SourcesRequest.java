package ru.spbau.mit.java.shared.request;

/**
 * Request for client ids, who are seeding file with id specified
 */
public class SourcesRequest {
    public static final byte code = 3;

    private final int fileId;

    public SourcesRequest(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) ||
                (obj instanceof SourcesRequest && ((SourcesRequest) obj).fileId == fileId);
    }

    @Override
    public String toString() {
        return "sources {file_id: " + fileId + "}";
    }
}
