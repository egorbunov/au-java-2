package ru.spbau.mit.java.shared;

import java.io.IOException;
import java.util.logging.Logger;

public class ServerSession implements Runnable {
    private Logger logger = Logger.getLogger(ServerSession.class.getName());
    private OneClientRequestServer requestServer;

    public ServerSession(OneClientRequestServer requestServer) {
        this.requestServer = requestServer;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                requestServer.serveOneRequest();
            }
        } catch (IOException e) {
            logger.severe("Error serving one request: " + e.getMessage());
        } finally {
            try {
                requestServer.disconnect();
            } catch (IOException e) {
                logger.severe("Error disconnecting request server");
            }
        }
    }
}
