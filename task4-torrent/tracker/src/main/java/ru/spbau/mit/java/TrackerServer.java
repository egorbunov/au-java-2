package ru.spbau.mit.java;

import ru.spbau.mit.java.shared.protocol.ServerTrackerProtocol;
import ru.spbau.mit.java.shared.protocol.ServerTrackerProtocolImp;
import ru.spbau.mit.java.shared.OneClientRequestServer;
import ru.spbau.mit.java.shared.SimpleServer;
import ru.spbau.mit.java.shared.error.SessionStartError;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class TrackerServer extends SimpleServer {
    private Tracker<ClientId, Integer> tracker;
    private long clientExitCheckPeriod;

    /**
     *
     * @param port port where server listens for requests
     * @param tracker tracker model
     * @param clientExitCheckPeriod period, in which tracker server checks
     *                              if client made an update request, if there was
     *                              no update request then client treated as disconnected
     *                              and all it's files info is deleted from tracker
     */
    public TrackerServer(int port, Tracker<ClientId, Integer> tracker, long clientExitCheckPeriod) {
        super("TrackerServer", port);
        this.tracker = tracker;
        this.clientExitCheckPeriod = clientExitCheckPeriod;
    }


    /**
     * Creates one tracker session for one client with given client-server data channel
     */
    @Override
    public Runnable createSession(Socket dataChannel) {
        InputStream dataIn;
        OutputStream dataOut;
        try {
            dataIn = dataChannel.getInputStream();
            dataOut = dataChannel.getOutputStream();
        } catch (IOException e) {
            throw new SessionStartError("Can't open client-tracker io streams");
        }

        ServerTrackerProtocol protocol = new ServerTrackerProtocolImp(dataIn, dataOut);

        TrackerRequestExecutor requestExecutor = new TrackerRequestExecutorImpl(
                dataChannel.getInetAddress().getAddress(),
                tracker
        );

        TrackerClientRequestServer requestServer =
                new TrackerClientRequestServer(dataChannel, protocol, requestExecutor);

        return () -> {
            TimedClientChecker timedTask = new TimedClientChecker(dataChannel.getInetAddress().getAddress(), requestServer);
            Thread timedTaskThread = new Thread(timedTask);
            timedTaskThread.start();
            while (!Thread.interrupted()) {
                logger.info("Waiting for request...");
                requestServer.serveOneRequest();
            }
            logger.info("Tracker server interrupted!");
            timedTaskThread.interrupt();
            try {
                logger.info("Disconnecting...");
                requestServer.disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.info("OK");
        };
    }


    /**
     * Task, which checks if client sends update request periodically
     */
    private class TimedClientChecker implements Runnable {
        private byte[] clientIp;
        private TrackerClientRequestServer requestServer;

        public TimedClientChecker(
                byte[] clientIp,
                TrackerClientRequestServer requestServer) {
            this.clientIp = clientIp;
            this.requestServer = requestServer;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                long beforeSleep = System.currentTimeMillis();
                try {
                    Thread.sleep(TrackerServer.this.clientExitCheckPeriod);
                } catch (InterruptedException e) {
                    break;
                }
                if (requestServer.getLastUpdate() <= beforeSleep) {
                    logger.info("Client update timeout");
                    if (requestServer.getClientSeedPort() > 0) {
                        tracker.removeClient(new ClientId(clientIp,
                                requestServer.getClientSeedPort()));
                    } else {
                        logger.info("negative client port...");
                    }
                }
            }
        }
    }
}
