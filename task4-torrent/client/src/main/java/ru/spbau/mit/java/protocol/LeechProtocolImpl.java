package ru.spbau.mit.java.protocol;


import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.IOException;

public class LeechProtocolImpl implements LeechProtocol {
    @Override
    public void writeStatRequest(StatRequest request) throws IOException {

    }

    @Override
    public StatResponse readStatResponse() throws IOException {
        return null;
    }

    @Override
    public void writeGetPartRequest(GetPartRequest request) throws IOException {

    }

    @Override
    public GetPartResponse readGetPartResponse() throws IOException {
        return null;
    }
}
