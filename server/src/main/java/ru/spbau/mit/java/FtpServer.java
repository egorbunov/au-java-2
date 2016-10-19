package ru.spbau.mit.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;


public class FtpServer {
    private Logger logger = Logger.getLogger("FtpServer");

    private final int port;
    private ServerSocket serverSocket;
    private AcceptingTask acceptingTask;
    private List<FtpSession> sessions = new ArrayList<>();

    public FtpServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            logger.severe("ERROR: can't create server socket at port " + port);
            return false;
        }

        acceptingTask = new AcceptingTask();
        new Thread(acceptingTask).start();

        return true;
    }

    public void stop() {
        logger.info("Stopping server...");
        acceptingTask.stop();

        logger.info("Aborting all sessions...");
        sessions.forEach(FtpSession::stop);

        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.severe("Can't close socket " + e.getMessage());
        }
    }


    /**
     * Task, which runs in infinite loop and accept connections
     * from clients.
     * For every connection new thread is started for processing it's requests
     */
    private class AcceptingTask implements Runnable {
        private boolean isRunning = true;

        @Override
        public void run() {
            while (isRunning) {
                Connection connection;
                try {
                    connection = performHandshake(serverSocket);
                    if (connection == null) {
                        logger.severe("ERROR: handshake failed");
                        continue;
                    }
                } catch (IOException e) {
                    logger.severe("ERROR [handshake]: " + e.getMessage());
                    continue;
                }

                logger.info("Creating new session...");
                FtpSession ftpSession;
                try {
                    ftpSession = new FtpSession(
                            connection.dataSocket.getInputStream(),
                            connection.dataSocket.getOutputStream(),
                            connection.statusSocket.getOutputStream()
                    );
                } catch (IOException e) {
                    logger.severe("ERROR: Can't create ftp session; " + e.getMessage());
                    continue;
                }

                sessions.add(ftpSession);

                logger.info("OK! Now running session thread.");
                new Thread(ftpSession).start();
            }
            if (isRunning) {
                logger.info("Accepting thread was stopped normally...");
            } else {
                logger.info("Accepting thread was stopped for some reason...");
            }
        }

        public void stop() {
            isRunning = false;
        }
    }

    /**
     * Establish connection on server side.
     * We need to establish 2 connections for data and status codes
     * so connect procedure is performed a little bit carefully:
     * 1) Accept some client connection (data channel)
     * 2) Send some "uniquely" generated identifier
     * 3) Accept second connection (we want it to be status channel for
     *    accepted at step 1 client)
     * 4) Check if id sent equal to id got
     */
    private Connection performHandshake(ServerSocket serverSocket) throws IOException {
        logger.info("Waiting for connection...");
        Socket dataChannel = serverSocket.accept();

        logger.info("Trying to perform handshake...");
        DataOutputStream out = new DataOutputStream(dataChannel.getOutputStream());
        long identifier = new Random(System.currentTimeMillis()).nextLong();
        logger.info("Handshake id = " + identifier);
        out.writeLong(identifier);

        logger.info("Waiting for status channel connection...");
        Socket statusChannel = serverSocket.accept();
        // statusChannel.setSoTimeout(1000);
        DataInputStream in = new DataInputStream(statusChannel.getInputStream());
        long gotId = in.readLong();
        logger.info("Verifying connection; Got id = " + gotId);
        if (gotId == identifier) {
            logger.info("Ids are equal! Ok! Notifying client, that connection established");
            out.writeBoolean(true);
            // statusChannel.setSoTimeout(0);
            return new Connection(dataChannel, statusChannel);
        }

        logger.info("Handshake failed.");
        out.writeBoolean(false);
        return null;
    }
}
