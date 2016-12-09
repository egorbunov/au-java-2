package ru.spbau.mit.java.seed;


import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.IOException;

/**
 * Logic for executing requests came from leech-clients.
 *
 * One may have multiple request executing logic for some reason,
 * so every such logic is implemented via implementation of this interface
 */
public interface LeecherRequestExecutor {
    StatResponse executeStat(StatRequest request);

    GetPartResponse executeGetPart(GetPartRequest request) throws IOException;
}
