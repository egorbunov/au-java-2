package ru.spbau.mit.java.seed;


import ru.spbau.mit.java.protocol.SeedProtocol;
import ru.spbau.mit.java.protocol.request.ClientRequestCode;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;
import ru.spbau.mit.java.shared.RequestServer;
import ru.spbau.mit.java.shared.error.ServeIOError;

import java.io.IOException;

/**
 * Class, which works with protocol and ensures that for every request
 * response is written (in case there is no exception...)
 */
public class SeedingRequestServer implements RequestServer {
    private final SeedProtocol protocol;
    private final SeedingRequestExecutor requestExecutor;

    /**
     * @param protocol rules for requests and responses serialization/deserialization
     * @param requestExecutor request processing logic
     */
    public SeedingRequestServer(SeedProtocol protocol,
                                SeedingRequestExecutor requestExecutor) {

        this.protocol = protocol;
        this.requestExecutor = requestExecutor;
    }

    @Override
    public void serveOneRequest() {
        try {
            ClientRequestCode code = protocol.readRequestCode();
            switch (code) {
                case STAT: {
                    StatRequest r = protocol.readStatRequest();
                    StatResponse response = requestExecutor.executeStat(r);
                    protocol.writeStatResponse(response);
                    break;
                }
                case GET: {
                    GetPartRequest r = protocol.readGetPartRequest();
                    GetPartResponse response = requestExecutor.executeGetPart(r);
                    protocol.writeGetPartResponse(response);
                    break;
                }
            }
        } catch (IOException e) {
            throw new ServeIOError(e);
        }

    }
}
