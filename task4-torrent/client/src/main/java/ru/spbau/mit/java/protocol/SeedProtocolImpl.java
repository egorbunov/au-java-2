package ru.spbau.mit.java.protocol;

import ru.spbau.mit.java.protocol.request.ClientRequestCode;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.IOException;


public class SeedProtocolImpl implements SeedProtocol {
    @Override
    public ClientRequestCode readRequestCode() throws IOException {
        return null;
    }

    @Override
    public StatRequest readStatRequest() throws IOException {
        return null;
    }

    @Override
    public void writeStatResponse(StatResponse response) throws IOException {

    }

    @Override
    public GetPartRequest readGetPartRequest() throws IOException {
        return null;
    }

    @Override
    public void writeGetPartResponse() throws IOException {

    }
}
