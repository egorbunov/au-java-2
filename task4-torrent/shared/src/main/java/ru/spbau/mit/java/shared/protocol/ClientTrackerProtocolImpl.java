package ru.spbau.mit.java.shared.protocol;

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
public class ClientTrackerProtocolImpl implements ClientTrackerProtocol {
    private final DataInputStream responseIn;
    private final DataOutputStream requestOut;

    public ClientTrackerProtocolImpl(InputStream responseIn, OutputStream requestOut) {
        this.responseIn = new DataInputStream(responseIn);
        this.requestOut = new DataOutputStream(requestOut);
    }

    @Override
    public void writeUpdateRequest(UpdateRequest r) throws IOException {
        requestOut.writeByte(UpdateRequest.code);
        requestOut.writeShort(r.getClientPort());
        requestOut.writeInt(r.getFileIds().size());
        for (Integer id : r.getFileIds()) {
            requestOut.writeInt(id);
        }
    }

    @Override
    public void writeUploadRequest(UploadRequest r) throws IOException {
        requestOut.writeByte(UploadRequest.code);
        requestOut.writeUTF(r.getName());
        requestOut.writeInt(r.getSize());
    }

    @Override
    public void writeSourcesRequest(SourcesRequest r) throws IOException {
        requestOut.writeByte(SourcesRequest.code);
        requestOut.writeInt(r.getFileId());
    }

    @Override
    public void writeListRequest(ListRequest r) throws IOException {
        requestOut.writeByte(ListRequest.code);
    }

    @Override
    public UpdateResponse readUpdateResponse() throws IOException {
        boolean status = responseIn.readBoolean();
        return new UpdateResponse(status);
    }

    @Override
    public UploadResponse readUploadResponse() throws IOException {
        int fileId = responseIn.readInt();
        return new UploadResponse(fileId);
    }

    @Override
    public ListResponse readListResponse() throws IOException {
        int count = responseIn.readInt();
        List<TrackerFile<Integer>> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int fileId = responseIn.readInt();
            String fileName = responseIn.readUTF();
            int fileSize = responseIn.readInt();
            files.add(new TrackerFile<>(fileId, fileName, fileSize));
        }
        return new ListResponse(files);
    }

    @Override
    public SourcesResponse readSourcesResponse() throws IOException {
        int count = responseIn.readInt();
        List<ClientId> clients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            byte[] ip = new byte[4];
            responseIn.readFully(ip);
            short clientPort = responseIn.readShort();
            clients.add(new ClientId(ip, clientPort));
        }
        return null;
    }
}
