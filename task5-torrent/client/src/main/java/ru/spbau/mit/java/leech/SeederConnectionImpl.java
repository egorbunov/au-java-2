package ru.spbau.mit.java.leech;


import ru.spbau.mit.java.files.error.FileNotExists;
import ru.spbau.mit.java.protocol.LeechProtocol;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Connection to one seeding client. So this code is executed by leecher.
 */
public class SeederConnectionImpl implements SeederConnection {
    private Logger logger = Logger.getLogger(SeederConnectionImpl.class.getSimpleName());
    private Socket connection;
    private final LeechProtocol protocol;

    /**
     *
     * @param connection socket, on which connection is performed
     * @param protocol protocol over connection (for correctness protocol
     *                 must use the same connection as passed in 1 parameter)
     */
    public SeederConnectionImpl(Socket connection, LeechProtocol protocol) {
        this.connection = connection;
        this.protocol = protocol;
    }

    /**
     * Returns collection of available file parts on seeder side of
     * the connection.
     *
     * @param fileId id of the file to stat
     * @return collection of file parts
     * @throws ru.spbau.mit.java.files.error.FileNotExists in case of no file with
     *         id specified available on the seeder side
     */
    @Override
    public Collection<Integer> stat(int fileId) throws IOException {
        logger.info("Sending stat request " + fileId);
        StatRequest r = new StatRequest(fileId);
        protocol.writeStatRequest(r);
        logger.info("Waiting for stat response");
        StatResponse response = protocol.readStatResponse();
        logger.info("Got response");
        if (response == null) {
            throw new FileNotExists(fileId);
        }
        return response.getPartIds();
    }

    /**
     * Download one file part (block) from client
     * @param fileId file identifier
     * @param blockId block in file id
     * @return part in bytes in case of success
     */
    @Override
    public byte[] downloadFileBlock(int fileId, int blockId) throws IOException {
        GetPartRequest r = new GetPartRequest(fileId, blockId);
        logger.info("Sending start request" + r);
        protocol.writeGetPartRequest(r);
        GetPartResponse response = protocol.readGetPartResponse();
        logger.info("Got response, byte cnt = " + response.getBytes().length);
        return response.getBytes();
    }

    @Override
    public void disconnect() throws IOException {
        connection.close();
    }
}

