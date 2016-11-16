package ru.spbau.mit.java.protocol;

import ru.spbau.mit.java.protocol.request.ClientRequestCode;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;
import ru.spbau.mit.java.shared.UnknownRequestCode;
import ru.spbau.mit.java.shared.request.*;

import java.io.*;


public class SeedProtocolImpl implements SeedProtocol {
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;

    /**
     *
     * @param dataIn stream, there to write responses
     * @param dataOut stream, from where to read requests
     */
    public SeedProtocolImpl(InputStream dataIn, OutputStream dataOut) {
        this.dataIn = new DataInputStream(dataIn);
        this.dataOut = new DataOutputStream(dataOut);
    }

    @Override
    public ClientRequestCode readRequestCode() throws IOException {
        byte code = dataIn.readByte();
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
        return new StatRequest(dataIn.readInt());
    }

    @Override
    public void writeStatResponse(StatResponse response) throws IOException {
        dataOut.writeInt(response.getPartIds().size());
        for (int partId : response.getPartIds()) {
            dataOut.writeInt(partId);
        }
    }

    @Override
    public GetPartRequest readGetPartRequest() throws IOException {
        int fileId = dataIn.readInt();
        int partId = dataIn.readInt();
        return new GetPartRequest(fileId, partId);
    }

    @Override
    public void writeGetPartResponse(GetPartResponse response) throws IOException {
        dataOut.write(response.getBytes());
    }
}
