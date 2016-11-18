package ru.spbau.mit.java.shared;

import ru.spbau.mit.java.shared.error.ServerShutdownError;
import ru.spbau.mit.java.shared.error.ServerStartupError;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;

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
public abstract class SimpleServer implements Server {
    protected final Logger logger;
    protected final List<ServerSession> sessions;

    private ServerSocket serverSocket;
    protected final String serverName;
    protected final int port;
    private Thread acceptingThread;

    public SimpleServer(String serverName, int port) {
        logger = Logger.getLogger(serverName);
        this.serverName = serverName;
        this.port = port;
        this.sessions = new ArrayList<>();
    }

    @Override
    final public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            logger.severe("ERROR: can't create server socket at port " + port);
            throw new ServerStartupError("Can't start server: " + serverName, e);
        }

        AcceptingTask acceptingTask = new AcceptingTask();
        acceptingThread = new Thread(acceptingTask);
        acceptingThread.start();
    }

    @Override
    final public void stop() {
        acceptingThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.severe("Can't close socket!");
            throw new ServerShutdownError("Can't close socket, server = " + serverName, e);
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
                InputStream dataIn;
                OutputStream dataOut;

                try {
                    logger.info("Waiting for connection...");
                    dataChannel = serverSocket.accept();
                    dataIn = dataChannel.getInputStream();
                    dataOut = dataChannel.getOutputStream();
                } catch (IOException e) {
                    logger.severe("Can't accept connection: " + e.getMessage());
                    continue;
                }

                ServerSession session = new ServerSession(
                        createSessionRequestServer(dataChannel, dataIn, dataOut)
                );

                sessions.add(session);

                logger.info("OK! Now running session thread.");
                new Thread(session).start();
            }
            logger.info("Accepting thread exit...");
        }
    }


    abstract public RequestServer createSessionRequestServer(Socket dataChannel,
                                                             InputStream dataIn,
                                                             OutputStream dataOut);
}


