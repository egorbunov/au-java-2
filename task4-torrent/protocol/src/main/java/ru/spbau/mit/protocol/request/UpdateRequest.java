package ru.spbau.mit.protocol.request;

import java.util.List;

public class UpdateRequest {
    public static final byte code = 4;

    private final int clientPort;
    private final List<Integer> fileIds;

    public UpdateRequest(int clientPort, List<Integer> fileIds) {
        this.clientPort = clientPort;
        this.fileIds = fileIds;
    }

    public int getClientPort() {
        return clientPort;
    }

    public List<Integer> getFileIds() {
        return fileIds;
    }
}
