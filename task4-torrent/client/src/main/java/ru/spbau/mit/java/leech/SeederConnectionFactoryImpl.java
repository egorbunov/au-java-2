package ru.spbau.mit.java.leech;


import ru.spbau.mit.java.protocol.LeechProtocol;
import ru.spbau.mit.java.protocol.LeechProtocolImpl;
import ru.spbau.mit.java.shared.tracker.ClientId;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class SeederConnectionFactoryImpl implements SeederConnectionFactory<ClientId>, Serializable {
    transient private Logger logger = Logger.getLogger(SeederConnectionFactoryImpl.class.getSimpleName());
    private int blockSizeInBytes;

    /**
     * @param blockSizeInBytes size in bytes of one file block
     */
    public SeederConnectionFactoryImpl(int blockSizeInBytes) {

        this.blockSizeInBytes = blockSizeInBytes;
    }

    @Override
    public SeederConnection connectToClient(ClientId clientId) throws IOException {
        Socket clientSocket = new Socket(InetAddress.getByAddress(clientId.getIp()), clientId.getPort());
        logger.info("Connected to client at " + InetAddress.getByAddress(clientId.getIp()).getHostAddress() +
                ":" + clientId.getPort());

        LeechProtocol protocol = new LeechProtocolImpl(clientSocket.getInputStream(),
                clientSocket.getOutputStream(),
                blockSizeInBytes);

        return new SeederConnectionImpl(clientSocket, protocol);
    }
}
