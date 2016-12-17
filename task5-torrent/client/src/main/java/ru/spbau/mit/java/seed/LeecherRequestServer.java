package ru.spbau.mit.java.seed;


import ru.spbau.mit.java.files.error.FileNotExistsInStorage;
import ru.spbau.mit.java.protocol.SeedProtocol;
import ru.spbau.mit.java.protocol.request.ClientRequestCode;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;
import ru.spbau.mit.java.shared.OneClientRequestServer;
import ru.spbau.mit.java.shared.error.ServerIOError;
import ru.spbau.mit.java.shared.error.UnknownRequestCode;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Leecher means, that requests come from leechers (clients, who want to download
 * from us something)
 * <p>
 * Class, which works with protocol and ensures that for every request
 * response is written (in case there is no exception...)
 */
public class LeecherRequestServer implements OneClientRequestServer {
    private Logger logger = Logger.getLogger(LeecherRequestServer.class.getName());
    private Socket connection;
    private final SeedProtocol protocol;
    private final LeecherRequestExecutor requestExecutor;

    /**
     * @param connection      leecher-seeder connection
     * @param protocol        rules for requests and responses serialization/deserialization
     * @param requestExecutor request processing logic
     */
    public LeecherRequestServer(Socket connection,
                                SeedProtocol protocol,
                                LeecherRequestExecutor requestExecutor) {
        this.connection = connection;
        this.protocol = protocol;
        this.requestExecutor = requestExecutor;
    }

    @Override
    public void serveOneRequest() throws IOException {
        ClientRequestCode code;
        try {
            code = protocol.readRequestCode();
        } catch (EOFException e) {
            logger.info("No more requests (got EOF)...returning...");
            Thread.currentThread().interrupt();
            return;
        } catch (UnknownRequestCode e) {
            logger.severe("Read bad request code. Returning");
            return;
        }
        switch (code) {
            case STAT: {
                logger.info("Reading stat request...");
                StatRequest r = protocol.readStatRequest();
                StatResponse response = null;
                response = requestExecutor.executeStat(r);
                logger.info("Writing stat response: " + response);
                protocol.writeStatResponse(response);
                break;
            }
            case GET: {
                logger.info("Reading get part request...");
                GetPartRequest r = protocol.readGetPartRequest();
                GetPartResponse response = null;
                response = requestExecutor.executeGetPart(r);
                logger.info("Writing get part response, bytes = " + response.getBytes().length +
                        "; part id = " + r.getPartId() + "; file_id = " + r.getFileId());
                protocol.writeGetPartResponse(response);
                break;
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (!connection.isClosed()) {
            connection.close();
        }
    }
}
