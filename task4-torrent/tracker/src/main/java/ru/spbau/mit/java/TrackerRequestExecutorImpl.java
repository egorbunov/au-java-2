package ru.spbau.mit.java;


import ru.spbau.mit.java.shared.request.ListRequest;
import ru.spbau.mit.java.shared.request.SourcesRequest;
import ru.spbau.mit.java.shared.request.UpdateRequest;
import ru.spbau.mit.java.shared.request.UploadRequest;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;


/**
 * Simple request executing logic, which delegates everything to Tracker object
 */
public class TrackerRequestExecutorImpl implements TrackerRequestExecutor {
    private byte[] clientIpAddress;
    private Tracker<ClientId, Integer> tracker;

    public TrackerRequestExecutorImpl(byte[] clientIpAddress,
                                      Tracker<ClientId, Integer> tracker) {
        this.clientIpAddress = clientIpAddress;
        this.tracker = tracker;
    }

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
}
