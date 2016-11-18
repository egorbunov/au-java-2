package ru.spbau.mit.java.seed;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.protocol.SeedProtocol;
import ru.spbau.mit.java.protocol.SeedProtocolImpl;
import ru.spbau.mit.java.shared.RequestServer;
import ru.spbau.mit.java.shared.Server;
import ru.spbau.mit.java.shared.SimpleServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Client side server; This server starts waiting for other
 * clients connections after start and accepts them to send them
 * file parts
 */
public class SeedingServer extends SimpleServer {
    private final FileBlocksStorage fileBlocksStorage;

    public SeedingServer(int port, FileBlocksStorage fileBlocksStorage) {
        super("SeedingServer", port);
        this.fileBlocksStorage = fileBlocksStorage;
    }

    @Override
    public RequestServer createSessionRequestServer(Socket dataChannel,
                                                    InputStream dataIn,
                                                    OutputStream dataOut) {
        SeedProtocol protocol = new SeedProtocolImpl(dataIn, dataOut);
        SeedingRequestExecutor executor = new SeedingRequestExecutorImpl(fileBlocksStorage);
        return new SeedingRequestServer(protocol, executor);
    }
}
