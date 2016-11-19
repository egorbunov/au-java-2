package ru.spbau.mit.java;

import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocol;
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
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Interface to tracker which resides on tracker-server side.
 * So every method performs request sending to the tracker and
 * waits for them being answered
 */
public class RemoteTracker implements Tracker<ClientId, Integer> {
    private ClientTrackerProtocol trackerProtocol;

    public RemoteTracker(ClientTrackerProtocol trackerProtocol) {
        this.trackerProtocol = trackerProtocol;
    }

    @Override
    public Collection<TrackerFile<Integer>> list() {
        try {
            trackerProtocol.writeListRequest(new ListRequest());
            ListResponse response = trackerProtocol.readListResponse();
            return response.getFiles();
        } catch (IOException e) {
            throw new RemoteTrackerError("list", e);
        }

    }

    @Override
    public void update(ClientId clientId, List<Integer> fileIds) {
        try {
            trackerProtocol.writeUpdateRequest(new UpdateRequest(clientId.getPort(), fileIds));
            UpdateResponse r = trackerProtocol.readUpdateResponse();
            if (!r.getStatus()) {
                throw new RuntimeException("update response bad status");
            }
        } catch (IOException e) {
            throw new RemoteTrackerError("update", e);
        }

    }


    @Override
    public Integer upload(FileInfo fileInfo) {
        try {
            trackerProtocol.writeUploadRequest(new UploadRequest(fileInfo.getName(), fileInfo.getSize()));
            UploadResponse r = trackerProtocol.readUploadResponse();
            return r.getFileId();
        } catch (IOException e) {
            throw new RemoteTrackerError("upload", e);
        }
    }

    @Override
    public Collection<ClientId> source(Integer fileId) {
        try {
            trackerProtocol.writeSourcesRequest(new SourcesRequest(fileId));
            SourcesResponse r = trackerProtocol.readSourcesResponse();
            return r.getClients();
        } catch (IOException e) {
            throw new RemoteTrackerError("sources", e);
        }
    }

    @Override
    public void removeClient(ClientId clientId) {
        // pass
    }
}
