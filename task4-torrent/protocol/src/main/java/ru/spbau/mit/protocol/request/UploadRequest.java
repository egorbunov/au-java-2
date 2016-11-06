package ru.spbau.mit.protocol.request;


public class UploadRequest {
    public static final byte code = 2;

    private final String name;
    private final int size;

    public UploadRequest(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}
