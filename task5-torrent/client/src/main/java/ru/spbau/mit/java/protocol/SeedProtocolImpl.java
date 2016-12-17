package ru.spbau.mit.java.protocol;

import ru.spbau.mit.java.protocol.request.ClientRequestCode;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;
import ru.spbau.mit.java.shared.error.UnknownRequestCode;

import java.io.*;


public class SeedProtocolImpl implements SeedProtocol {
    private final DataInputStream requestIn;
    private final DataOutputStream responseOut;

    /**
     *
     * @param requestIn stream, there to write responses
     * @param responseOut stream, from where to read requests
     */
    public SeedProtocolImpl(InputStream requestIn, OutputStream responseOut) {
        this.requestIn = new DataInputStream(requestIn);
        this.responseOut = new DataOutputStream(responseOut);
    }

    @Override
    public ClientRequestCode readRequestCode() throws IOException, UnknownRequestCode {
        byte code = requestIn.readByte();
        switch (code) {
            case StatRequest.code:
                return ClientRequestCode.STAT;
            case GetPartRequest.code:
                return ClientRequestCode.GET;
            default:
                throw new UnknownRequestCode(code);
        }
    }

    @Override
    public StatRequest readStatRequest() throws IOException {
        return new StatRequest(requestIn.readInt());
    }

    @Override
    public void writeStatResponse(StatResponse response) throws IOException {
        responseOut.writeInt(response.getPartIds().size());
        for (int partId : response.getPartIds()) {
            responseOut.writeInt(partId);
        }
    }

    @Override
    public GetPartRequest readGetPartRequest() throws IOException {
        int fileId = requestIn.readInt();
        int partId = requestIn.readInt();
        return new GetPartRequest(fileId, partId);
    }

    @Override
    public void writeGetPartResponse(GetPartResponse response) throws IOException {
        responseOut.write(response.getBytes());
        responseOut.flush();
    }
}
