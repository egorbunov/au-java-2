package ru.spbau.mit.java.shared.tracker;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClientId)) {
            return false;
        }
        ClientId other = (ClientId) obj;
        return Arrays.equals(other.ip, ip) && other.port == port;
    }
}
