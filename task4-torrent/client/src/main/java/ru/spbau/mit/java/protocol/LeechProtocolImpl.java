package ru.spbau.mit.java.protocol;


import ru.spbau.mit.java.files.FileBlocksStorage;
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
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;
    private final int partSizeInBytes;

    /**
     *
     * @param dataIn stream, there to write requests
     * @param dataOut stream, from where to read responses
     * @param partSizeInBytes one file part size in bytes
     */
    public LeechProtocolImpl(InputStream dataIn, OutputStream dataOut, int partSizeInBytes) {
        this.dataIn = new DataInputStream(dataIn);
        this.dataOut = new DataOutputStream(dataOut);
        this.partSizeInBytes = partSizeInBytes;
    }

    @Override
    public void writeStatRequest(StatRequest request) throws IOException {
        dataOut.writeByte(StatRequest.code);
        dataOut.writeInt(request.getFileId());
    }

    @Override
    public StatResponse readStatResponse() throws IOException {
        int cnt = dataIn.readInt();
        if (cnt < 0) {
            return null;
        }
        List<Integer> parts = new ArrayList<>(cnt);
        for (int i = 0; i < cnt; ++i) {
            parts.add(dataIn.readInt());
        }
        return new StatResponse(parts);
    }

    @Override
    public void writeGetPartRequest(GetPartRequest request) throws IOException {
        dataOut.writeByte(GetPartRequest.code);
        dataOut.writeInt(request.getFileId());
        dataOut.writeInt(request.getPartId());
    }

    @Override
    public GetPartResponse readGetPartResponse() throws IOException {
        byte[] part = new byte[partSizeInBytes];
        int readCnt = 0;
        while (readCnt != partSizeInBytes) {
            readCnt += dataIn.read(part, readCnt, part.length - readCnt);
        }
        return new GetPartResponse(part);
    }
}
