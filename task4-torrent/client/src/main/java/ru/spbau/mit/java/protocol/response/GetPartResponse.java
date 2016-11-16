package ru.spbau.mit.java.protocol.response;


public class GetPartResponse {
    private final byte[] bytes;

    public GetPartResponse(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
