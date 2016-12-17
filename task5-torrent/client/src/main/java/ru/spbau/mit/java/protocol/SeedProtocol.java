package ru.spbau.mit.java.protocol;

import ru.spbau.mit.java.protocol.request.ClientRequestCode;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;
import ru.spbau.mit.java.shared.error.UnknownRequestCode;

import java.io.IOException;

/**
 * Protocol which used by seeding part of the client to response
 * for leeching clients requests
 */
public interface SeedProtocol {
    ClientRequestCode readRequestCode() throws IOException, UnknownRequestCode;

    StatRequest readStatRequest() throws IOException;

    void writeStatResponse(StatResponse response) throws IOException;

    GetPartRequest readGetPartRequest() throws IOException;

    void writeGetPartResponse(GetPartResponse response) throws IOException;
}
