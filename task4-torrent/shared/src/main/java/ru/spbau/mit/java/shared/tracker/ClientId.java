package ru.spbau.mit.java.shared.tracker;


import java.io.Serializable;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;

public class ClientId implements Serializable, Comparable<ClientId> {
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

    @Override
    public String toString() {
        try {
            return InetAddress.getByAddress(ip).toString() + ":" + Short.toString(port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(ClientId o) {
        return this.toString().compareTo(o.toString());
    }
}
