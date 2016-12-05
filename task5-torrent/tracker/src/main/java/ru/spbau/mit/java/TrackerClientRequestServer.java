package ru.spbau.mit.java;


import ru.spbau.mit.java.shared.protocol.ServerTrackerProtocol;
import ru.spbau.mit.java.shared.OneClientRequestServer;
import ru.spbau.mit.java.shared.error.ServeIOError;
import ru.spbau.mit.java.shared.request.*;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * TrackerClientRequestServer reads request using given protocol
 * and serves this request using request executor.
 *
 * It also ensures, that every request
 * gets it response in case of no exceptions occurs
 * (so it acts like part protocol a little bit)
 *
 * For every client exactly one request server must be created
 */
public class TrackerClientRequestServer implements OneClientRequestServer {
    private Logger logger = Logger.getLogger(TrackerClientRequestServer.class.getSimpleName());
    private Socket connection;
    private final ServerTrackerProtocol trackerProtocol;
    private final TrackerRequestExecutor requestExecutor;
    private volatile long lastUpdate = 0;
    private volatile short clientSeedPort = -1;

    /**
     *
     * @param trackerProtocol rules for writing/reading requests/responses
     * @param requestExecutor request executing logic
     */
    public TrackerClientRequestServer(Socket connection,
                                      ServerTrackerProtocol trackerProtocol,
                                      TrackerRequestExecutor requestExecutor) {
        this.connection = connection;
        this.trackerProtocol = trackerProtocol;
        this.requestExecutor = requestExecutor;
    }

    /**
     * Main method. It firstly tries to read request and after that calls
     * request executor tp serve it...
     */
    @Override
    public void serveOneRequest() {
        try {
            RequestCode requestCode;
            try {
                requestCode = trackerProtocol.readRequestCode();
            } catch (EOFException e) {
                logger.info("No more requests (got EOF)...returning...");
                Thread.currentThread().interrupt();
                return;
            }
            switch (requestCode) {
                case UPDATE: {
                    lastUpdate = System.currentTimeMillis();
                    UpdateRequest r = trackerProtocol.readUpdateRequest();
                    logger.info("Got request " + r);
                    if (r.getClientPort() != clientSeedPort) {
                        logger.info("Client seed port change: " + clientSeedPort + " -> " + r.getClientPort());
                    }

                    clientSeedPort = r.getClientPort();

                    UpdateResponse response = requestExecutor.executeUpdate(r);
                    trackerProtocol.writeUpdateResponse(response);
                    break;
                }
                case UPLOAD: {
                    UploadRequest r = trackerProtocol.readUploadRequest();
                    logger.info("Got request " + r);

                    UploadResponse response = requestExecutor.executeUpload(r);
                    trackerProtocol.writeUploadResponse(response);
                    break;
                }
                case SOURCES: {
                    SourcesRequest r = trackerProtocol.readSourcesRequest();
                    logger.info("Got request " + r);

                    SourcesResponse response = requestExecutor.executeSource(r);
                    trackerProtocol.writeSourcesResponse(response);
                    break;
                }
                case LIST: {
                    ListRequest r = trackerProtocol.readListRequest();
                    logger.info("Got request " + r);

                    ListResponse response = requestExecutor.executeList(r);
                    trackerProtocol.writeListResponse(response);
                    break;
                }
            }
        } catch (IOException e) {
            throw new ServeIOError(e.getCause());
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (!connection.isClosed()) {
            connection.close();
        }
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public short getClientSeedPort() {
        return clientSeedPort;
    }
}
