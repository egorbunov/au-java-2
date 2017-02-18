package ru.spbau.mit.java.shared;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolServer {
    private final ExecutorService tp;
    private final String serverName;
    private final int port;

    public ThreadPoolServer(String serverName, int port) {
        this.tp = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.serverName = serverName;
        this.port = port;
    }
}
