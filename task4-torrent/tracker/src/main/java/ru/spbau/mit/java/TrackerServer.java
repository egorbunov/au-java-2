package ru.spbau.mit.java;

import ru.spbau.mit.java.protocol.TrackerProtocol;
import ru.spbau.mit.java.protocol.TrackerProtocolImp;
import ru.spbau.mit.java.shared.OneClientRequestServer;
import ru.spbau.mit.java.shared.SimpleServer;
import ru.spbau.mit.java.shared.error.SessionStartError;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class TrackerServer extends SimpleServer {
    private Tracker<ClientId, Integer> tracker;

    public TrackerServer(int port, Tracker<ClientId, Integer> tracker) {
        super("TrackerServer", port);
        this.tracker = tracker;
    }


    @Override
    public OneClientRequestServer createSessionRequestServer(Socket dataChannel) {
        InputStream dataIn;
        OutputStream dataOut;
        try {
            dataIn = dataChannel.getInputStream();
            dataOut = dataChannel.getOutputStream();
        } catch (IOException e) {
            throw new SessionStartError("Can't open client-tracker io streams");
        }

        TrackerProtocol protocol = new TrackerProtocolImp(dataIn, dataOut);

        TrackerRequestExecutor requestExecutor = new TrackerRequestExecutorImpl(
                dataChannel.getInetAddress().getAddress(),
                tracker
        );

        return new TrackerClientRequestServer(dataChannel, protocol, requestExecutor);
    }
}
