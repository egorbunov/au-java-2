package ru.spbau.mit.java.shared;

public class ServerSession implements Runnable {
    private RequestServer requestServer;

    public ServerSession(RequestServer requestServer) {
        this.requestServer = requestServer;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            requestServer.serveOneRequest();
        }
    }
}
