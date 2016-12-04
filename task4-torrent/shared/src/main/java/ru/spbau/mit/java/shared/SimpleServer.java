package ru.spbau.mit.java.shared;

import ru.spbau.mit.java.shared.error.ServerShutdownError;
import ru.spbau.mit.java.shared.error.ServerStartupError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base class for simple server, which may be started and stopped.
 * This server on start runs thread, which loops and accepts client
 * connections ({@link SimpleServer.AcceptingTask}. In this accepting
 * task
 */
public abstract class SimpleServer {
    protected final Logger logger;
    protected final List<Thread> sessions;

    private ServerSocket serverSocket;
    protected final String serverName;
    protected final int port;
    private Thread acceptingThread;

    public SimpleServer(String serverName, int port) {
        logger = Logger.getLogger(SimpleServer.class.getName());
        this.serverName = serverName;
        this.port = port;
        this.sessions = new ArrayList<>();
    }

    final public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            logger.severe("[" + serverName + "] ERROR: can't create server socket at port " + port);
            throw new ServerStartupError("Can't start server: " + serverName, e);
        }

        AcceptingTask acceptingTask = new AcceptingTask();
        acceptingThread = new Thread(acceptingTask);
        acceptingThread.start();
    }

    final public void stop() {
        acceptingThread.interrupt();
        try {
            serverSocket.close();
            sessions.forEach(Thread::interrupt);
        } catch (IOException e) {
            logger.severe("[" + serverName + "] Can't close socket or connections!");
            throw new ServerShutdownError("server = " + serverName, e);
        }
    }

    /**
     * Task, which runs in infinite loop and accept connections
     * from clients.
     * For every connection new thread is started for processing it's requests
     */
    private class AcceptingTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Socket dataChannel;

                try {
                    logger.info("[" + serverName + "] Waiting for connection...");
                    dataChannel = serverSocket.accept();
                } catch (IOException e) {
                    logger.severe("[" + serverName + "] Can't accept connection: " + e.getMessage());
                    continue;
                }

                Runnable session = createSession(dataChannel);
                Thread sessionThread = new Thread(session);

                sessions.add(sessionThread);
                logger.info("[" + serverName + "] OK! Now running session thread.");
                sessionThread.start();
            }
            logger.info("[" + serverName + "] Accepting thread exit...");
        }
    }


    abstract public Runnable createSession(Socket dataChannel);
}


