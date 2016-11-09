package ru.spbau.mit.java;

import ru.spbau.mit.java.shared.RequestServer;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.request.ListRequest;
import ru.spbau.mit.java.shared.request.SourcesRequest;
import ru.spbau.mit.java.shared.request.UpdateRequest;
import ru.spbau.mit.java.shared.request.UploadRequest;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.ClientId;

import java.util.function.Function;


/**
 * One client-server session
 */
public class ClientServerSession implements Runnable {
    private final RequestServer requestServer;

    /**
     *
     * @param clientIpAddress 4 byte representation of client ip address
     * @param tracker torrent tracker, which may be queried/changed by serving
     *                requests during session
     * @param serverProducer producer of request serving object given request executor
     */
    public ClientServerSession(byte[] clientIpAddress,
                               Tracker<ClientId, Integer> tracker,
                               Function<TrackerRequestExecutor, RequestServer> serverProducer) {

        this.requestServer = serverProducer.apply(new TrackerRequestExecutor() {
            @Override
            public UpdateResponse executeUpdate(UpdateRequest r) {
                tracker.update(new ClientId(clientIpAddress, r.getClientPort()),
                               r.getFileIds());
                return new UpdateResponse(true);
            }

            @Override
            public UploadResponse executeUpload(UploadRequest r) {
                int fileId = tracker.upload(new FileInfo(r.getSize(), r.getName()));
                return new UploadResponse(fileId);
            }

            @Override
            public SourcesResponse executeSource(SourcesRequest r) {
                return new SourcesResponse(tracker.source(r.getFileId()));
            }

            @Override
            public ListResponse executeList(ListRequest r) {
                return new ListResponse(tracker.list());
            }
        });
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            requestServer.serveOneRequest();
        }
    }
}
