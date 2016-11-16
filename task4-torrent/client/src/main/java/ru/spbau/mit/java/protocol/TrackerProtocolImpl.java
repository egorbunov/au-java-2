package ru.spbau.mit.java.protocol;

import ru.spbau.mit.java.shared.request.ListRequest;
import ru.spbau.mit.java.shared.request.SourcesRequest;
import ru.spbau.mit.java.shared.request.UpdateRequest;
import ru.spbau.mit.java.shared.request.UploadRequest;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Straightforward client-side protocol for talking to tracker
 * server implementation with data streams.
 *
 */
public class TrackerProtocolImpl implements TrackerProtocol {
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;

    public TrackerProtocolImpl(InputStream dataIn, OutputStream dataOut) {
        this.dataIn = new DataInputStream(dataIn);
        this.dataOut = new DataOutputStream(dataOut);
    }

    @Override
    public void writeUpdateRequest(UpdateRequest r) throws IOException {
        dataOut.writeByte(UpdateRequest.code);
        dataOut.writeShort(r.getClientPort());
        dataOut.writeInt(r.getFileIds().size());
        for (Integer id : r.getFileIds()) {
            dataOut.writeInt(id);
        }
    }

    @Override
    public void writeUploadRequest(UploadRequest r) throws IOException {
        dataOut.writeByte(UploadRequest.code);
        dataOut.writeUTF(r.getName());
        dataOut.writeInt(r.getSize());
    }

    @Override
    public void writeSourcesRequest(SourcesRequest r) throws IOException {
        dataOut.writeByte(SourcesRequest.code);
        dataOut.writeInt(r.getFileId());
    }

    @Override
    public void writeListRequest(ListRequest r) throws IOException {
        dataOut.writeByte(ListRequest.code);
    }

    @Override
    public UpdateResponse readUpdateResponse() throws IOException {
        boolean status = dataIn.readBoolean();
        return new UpdateResponse(status);
    }

    @Override
    public UploadResponse readUploadResponse() throws IOException {
        int fileId = dataIn.readInt();
        return new UploadResponse(fileId);
    }

    @Override
    public ListResponse readListResponse() throws IOException {
        int count = dataIn.readInt();
        List<TrackerFile<Integer>> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int fileId = dataIn.readInt();
            String fileName = dataIn.readUTF();
            int fileSize = dataIn.readInt();
            files.add(new TrackerFile<>(fileId, fileName, fileSize));
        }
        return new ListResponse(files);
    }

    @Override
    public SourcesResponse readSourcesResponse() throws IOException {
        int count = dataIn.readInt();
        List<ClientId> clients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            byte[] ip = new byte[4];
            dataIn.readFully(ip);
            short clientPort = dataIn.readShort();
            clients.add(new ClientId(ip, clientPort));
        }
        return null;
    }
}
