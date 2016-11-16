package ru.spbau.mit.java;


import ru.spbau.mit.java.protocol.TrackerProtocol;
import ru.spbau.mit.java.protocol.TrackerProtocolImp;
import ru.spbau.mit.java.shared.Server;
import ru.spbau.mit.java.shared.ServerSession;
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

public class TrackerServer implements Server {
    private final Logger logger = Logger.getLogger("TrackerServer");
    private final List<ServerSession> sessions;

    private ServerSocket serverSocket;
    private final int port;
    private Tracker<ClientId, Integer> tracker;
    private Thread acceptingThread;

    public TrackerServer(int port, Tracker<ClientId, Integer> tracker) {
        this.port = port;
        this.tracker = tracker;
        this.sessions = new ArrayList<>();
    }

    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            logger.severe("ERROR: can't create server socket at port " + port);
            throw new RuntimeException(e); // TODO: create separate exception
        }

        AcceptingTask acceptingTask = new AcceptingTask();
        acceptingThread = new Thread(acceptingTask);
        acceptingThread.start();
    }

    @Override
    public void stop() {
        acceptingThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.severe("Can't close socket!");
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

                TrackerProtocol protocol = new TrackerProtocolImp(dataIn, dataOut);

                TrackerRequestExecutor requestExecutor = new TrackerRequestExecutorImpl(
                        dataChannel.getInetAddress().getAddress(),
                        tracker
                );

                ServerSession session = new ServerSession(new TrackerRequestServer(protocol, requestExecutor));
                sessions.add(session);

                logger.info("OK! Now running session thread.");
                new Thread(session).start();
            }
            logger.info("Accepting thread exit...");
        }
    }
}
