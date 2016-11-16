package ru.spbau.mit.java.shared.request;

import java.util.List;


/**
 * Request, which client sends to server to tell him
 * which files (by id, got from tracker as a result of upload request)
 * he is seeding and on which port.
 */
public class UpdateRequest {
    public static final byte code = 4;

    private final short clientPort;
    private final List<Integer> fileIds;

    public UpdateRequest(short clientPort, List<Integer> fileIds) {
        this.clientPort = clientPort;
        this.fileIds = fileIds;
    }

    public short getClientPort() {
        return clientPort;
    }

    public List<Integer> getFileIds() {
        return fileIds;
    }
}
