package ru.spbau.mit.java.seed;


import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.IOException;

/**
 * One may have multiple request executing logics for some reason,
 * so every such logic is implemented via implementation of this interface
 */
public interface SeedingRequestExecutor {
    StatResponse executeStat(StatRequest request);

    GetPartResponse executeGetPart(GetPartRequest request) throws IOException;
}
