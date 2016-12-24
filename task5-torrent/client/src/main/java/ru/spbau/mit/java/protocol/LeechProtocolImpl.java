package ru.spbau.mit.java.protocol;


import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of leech protocol with data streams
 */
public class LeechProtocolImpl implements LeechProtocol {
    private final DataInputStream responseIn;
    private final DataOutputStream requestOut;
    private final int partSizeInBytes;

    /**
     *
     * @param responseIn stream, there to write requests
     * @param requestOut stream, from where to read responses
     * @param partSizeInBytes one file part size in bytes
     */
    public LeechProtocolImpl(InputStream responseIn, OutputStream requestOut, int partSizeInBytes) {
        this.responseIn = new DataInputStream(responseIn);
        this.requestOut = new DataOutputStream(requestOut);
        this.partSizeInBytes = partSizeInBytes;
    }

    @Override
    public void writeStatRequest(StatRequest request) throws IOException {
        requestOut.writeByte(StatRequest.code);
        requestOut.writeInt(request.getFileId());
    }

    @Override
    public StatResponse readStatResponse() throws IOException {
        int cnt = responseIn.readInt();
        if (cnt < 0) {
            return null;
        }
        List<Integer> parts = new ArrayList<>(cnt);
        for (int i = 0; i < cnt; ++i) {
            parts.add(responseIn.readInt());
        }
        return new StatResponse(parts);
    }

    @Override
    public void writeGetPartRequest(GetPartRequest request) throws IOException {
        requestOut.writeByte(GetPartRequest.code);
        requestOut.writeInt(request.getFileId());
        requestOut.writeInt(request.getPartId());
    }

    @Override
    public GetPartResponse readGetPartResponse() throws IOException {
        byte[] part = new byte[partSizeInBytes];
        int readCnt = 0;
        while (readCnt != partSizeInBytes) {
            readCnt += responseIn.read(part, readCnt, part.length - readCnt);
        }
        return new GetPartResponse(part);
    }
}
