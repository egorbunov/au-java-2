package ru.spbau.mit.protocol.tracker;


public class ClientId {
    private final byte[] ip;
    private final int port;

    public ClientId(byte[] ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public byte[] getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
