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

    public TrackerServer(int port, Tracker<ClientId, Integer> tracker) {
        super("TrackerServer", port);
        this.tracker = tracker;
    }


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
                requestServer.serveOneRequest();
            }
            timedTaskThread.interrupt();
            try {
                requestServer.disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
                    Thread.sleep(TimeUnit.MINUTES.toMillis(5));
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
