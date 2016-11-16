package ru.spbau.mit.java.protocol;

import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.IOException;

/**
 * Protocol which is used by leecher who queries other clients
 * for file parts.
 * This class just aggregates methods for writing requests and reading
 * responses.
 */
public interface LeechProtocol {
    void writeStatRequest(StatRequest request) throws IOException;

    StatResponse readStatResponse() throws IOException;

    void writeGetPartRequest(GetPartRequest request) throws IOException;

    GetPartResponse readGetPartResponse() throws IOException;
}
