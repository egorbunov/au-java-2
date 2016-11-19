package ru.spbau.mit.java.shared;

import java.io.IOException;

public class ServerSession implements Runnable {
    private OneClientRequestServer requestServer;

    public ServerSession(OneClientRequestServer requestServer) {
        this.requestServer = requestServer;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            requestServer.serveOneRequest();
        }
        try {
            requestServer.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
