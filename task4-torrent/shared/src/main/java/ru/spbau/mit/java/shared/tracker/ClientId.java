package ru.spbau.mit.java.shared.tracker;


import java.io.Serializable;

public class ClientId implements Serializable {
    private final byte[] ip;
    private final short port;

    public ClientId(byte[] ip, short port) {
        this.ip = ip;
        this.port = port;
    }

    public byte[] getIp() {
        return ip;
    }

    public short getPort() {
        return port;
    }
}
