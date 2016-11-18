package ru.spbau.mit.java;

import ru.spbau.mit.java.protocol.TrackerProtocol;
import ru.spbau.mit.java.protocol.TrackerProtocolImp;
import ru.spbau.mit.java.shared.RequestServer;
import ru.spbau.mit.java.shared.Server;
import ru.spbau.mit.java.shared.ServerSession;
import ru.spbau.mit.java.shared.SimpleServer;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class TrackerServer extends SimpleServer {
    private Tracker<ClientId, Integer> tracker;

    public TrackerServer(int port, Tracker<ClientId, Integer> tracker) {
        super("TrackerServer", port);
        this.tracker = tracker;
    }


    @Override
    public RequestServer createSessionRequestServer(Socket dataChannel,
                                                    InputStream dataIn,
                                                    OutputStream dataOut) {
        TrackerProtocol protocol = new TrackerProtocolImp(dataIn, dataOut);

        TrackerRequestExecutor requestExecutor = new TrackerRequestExecutorImpl(
                dataChannel.getInetAddress().getAddress(),
                tracker
        );

        return new TrackerRequestServer(protocol, requestExecutor);
    }
}
