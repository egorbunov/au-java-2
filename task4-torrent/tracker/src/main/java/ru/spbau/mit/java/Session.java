package ru.spbau.mit.java;

import ru.spbau.mit.java.protocol.RequestProtocol;
import ru.spbau.mit.java.protocol.ResponseProtocol;
import ru.spbau.mit.java.tracker.FileInfo;
import ru.spbau.mit.java.tracker.Tracker;
import ru.spbau.mit.protocol.request.*;
import ru.spbau.mit.protocol.response.*;
import ru.spbau.mit.protocol.tracker.ClientId;
import ru.spbau.mit.protocol.tracker.TrackerFile;

import java.util.Collection;


/**
 * One client-server session
 */
public class Session implements Runnable {
    private byte[] clientIpAddress;
    private final Tracker<ClientId, Integer> tracker;
    private final RequestProtocol requestProtocol;
    private final ResponseProtocol responseProtocol;

    public Session(byte[] clientIpAddress,
                   Tracker<ClientId, Integer> tracker,
                   RequestProtocol requestProtocol,
                   ResponseProtocol responseProtocol) {
        this.clientIpAddress = clientIpAddress;
        this.tracker = tracker;
        this.requestProtocol = requestProtocol;
        this.responseProtocol = responseProtocol;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            byte requestCode = requestProtocol.readRequestCode();
            switch (requestCode) {
                case UpdateRequest.code:
                    serveUpdateRequest();
                    break;
                case UploadRequest.code:
                    serveUploadRequest();
                    break;
                case ListRequest.code:
                    serveListRequest();
                    break;
                case SourcesRequest.code:
                    serveSourcesRequest();
                    break;
                default:
                    System.err.println("Error: got bad request code.");
                    break;
            }
        }
    }

    private void serveSourcesRequest() {
        SourcesRequest r = requestProtocol.readSourcesRequest();
        Collection<ClientId> seedIds = tracker.source(r.getFileId());
        responseProtocol.writeSourcesResponse(new SourcesResponse(seedIds));
    }

    private void serveListRequest() {
        requestProtocol.readListRequest();
        Collection<TrackerFile<Integer>> files = tracker.list();
        responseProtocol.writeListResponse(new ListResponse(files));
    }

    private void serveUploadRequest() {
        UploadRequest r = requestProtocol.readUploadRequest();
        int fileId = tracker.upload(new FileInfo(r.getSize(), r.getName()));
        responseProtocol.writeUploadResponse(new UploadResponse(fileId));
    }

    private void serveUpdateRequest() {
        UpdateRequest r = requestProtocol.readUpdateRequest();
        tracker.update(new ClientId(clientIpAddress, r.getClientPort()), r.getFileIds());
        responseProtocol.writeUpdateResponse(new UpdateResponse(true));
    }
}
