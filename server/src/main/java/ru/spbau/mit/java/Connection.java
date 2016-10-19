package ru.spbau.mit.java;

import java.net.Socket;

/**
 * Contains of sockets representing data and status
 * channels of Connection
 */
public class Connection {
    public final Socket dataSocket;
    public final Socket statusSocket;

    public Connection(Socket dataSocket, Socket statusSocket) {
        this.dataSocket = dataSocket;
        this.statusSocket = statusSocket;
    }
}
