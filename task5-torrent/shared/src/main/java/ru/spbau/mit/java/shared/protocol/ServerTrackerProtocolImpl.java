package ru.spbau.mit.java.shared.protocol;

import ru.spbau.mit.java.shared.error.UnknownRequestCode;
import ru.spbau.mit.java.shared.request.*;
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
 * Straightforward server-side tracker protocol implementation with
 * data streams
 */
public class ServerTrackerProtocolImpl implements ServerTrackerProtocol {
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;

    public ServerTrackerProtocolImpl(InputStream dataIn, OutputStream dataOut) {
        this.dataIn = new DataInputStream(dataIn);
        this.dataOut = new DataOutputStream(dataOut);
    }

    /**
     * @throws UnknownRequestCode if read request code is unknown!
     */
    @Override
    public RequestCode readRequestCode() throws IOException, UnknownRequestCode {
        byte code = dataIn.readByte();
        switch (code) {
            case UpdateRequest.code:
                return RequestCode.UPDATE;
            case UploadRequest.code:
                return RequestCode.UPLOAD;
            case SourcesRequest.code:
                return RequestCode.SOURCES;
            case ListRequest.code:
                return RequestCode.LIST;
            default:
                throw new UnknownRequestCode(code);
        }
    }

    @Override
    public UpdateRequest readUpdateRequest() throws IOException {
        short clientPort = dataIn.readShort();
        int fileCnt = dataIn.readInt();
        List<Integer> fileIds = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            fileIds.add(dataIn.readInt());
        }
        return new UpdateRequest(clientPort, fileIds);
    }

    @Override
    public UploadRequest readUploadRequest() throws IOException {
        String fileName = dataIn.readUTF();
        int fileSize = dataIn.readInt();
        return new UploadRequest(fileName, fileSize);
    }

    @Override
    public SourcesRequest readSourcesRequest() throws IOException {
        return new SourcesRequest(dataIn.readInt());
    }

    @Override
    public ListRequest readListRequest() {
        return new ListRequest();
    }

    @Override
    public void writeUpdateResponse(UpdateResponse r) throws IOException {
        dataOut.writeBoolean(r.getStatus());
    }

    @Override
    public void writeUploadResponse(UploadResponse r) throws IOException {
        dataOut.writeInt(r.getFileId());
    }

    @Override
    public void writeListResponse(ListResponse r) throws IOException {
        dataOut.writeInt(r.getFiles().size());
        for (TrackerFile<Integer> f : r.getFiles()) {
            dataOut.writeInt(f.getId());
            dataOut.writeUTF(f.getName());
            dataOut.writeInt(f.getSize());
        }
    }

    @Override
    public void writeSourcesResponse(SourcesResponse r) throws IOException {
        dataOut.writeInt(r.getClients().size());
        for (ClientId id : r.getClients()) {
            dataOut.writeBytes(new String(id.getIp()));
            dataOut.writeShort(id.getPort());
        }
    }
}
