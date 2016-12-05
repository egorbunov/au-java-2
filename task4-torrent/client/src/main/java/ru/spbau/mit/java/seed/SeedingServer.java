package ru.spbau.mit.java.seed;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.protocol.SeedProtocol;
import ru.spbau.mit.java.protocol.SeedProtocolImpl;
import ru.spbau.mit.java.shared.ServerSession;
import ru.spbau.mit.java.shared.SimpleServer;
import ru.spbau.mit.java.shared.error.SessionStartError;

import java.io.IOException;
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
    public Runnable createSession(Socket dataChannel) {
        InputStream dataIn;
        OutputStream dataOut;
        try {
            dataIn = dataChannel.getInputStream();
            dataOut = dataChannel.getOutputStream();
        } catch (IOException e) {
            throw new SessionStartError("Can't start leech-seed session");
        }

        SeedProtocol protocol = new SeedProtocolImpl(dataIn, dataOut);
        SeedingRequestExecutor executor = new SeedingRequestExecutorImpl(fileBlocksStorage);
        LeecherRequestServer requestServer =
                new LeecherRequestServer(dataChannel, protocol, executor);

        return new ServerSession(requestServer);
    }
}
