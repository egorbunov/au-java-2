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
import java.util.List;
import java.util.logging.Logger;

/**
 * Interface to tracker which resides on tracker-server side.
 * So every method performs request sending to the tracker and
 * waits for them being answered
 */
public class RemoteTracker implements Tracker<ClientId, Integer> {
    private final Logger logger;
    private ClientTrackerProtocol trackerProtocol;

    public RemoteTracker(ClientTrackerProtocol trackerProtocol) {
        this.trackerProtocol = trackerProtocol;
        logger = Logger.getLogger(RemoteTracker.class.getName());
    }

    @Override
    public List<TrackerFile<Integer>> list() {
        try {
            logger.info("Making list request...");
            trackerProtocol.writeListRequest(new ListRequest());
            ListResponse r = trackerProtocol.readListResponse();
            logger.info("Got response: " + r.toString());
            return r.getFiles();
        } catch (IOException e) {
            throw new RemoteTrackerError("list", e);
        }

    }

    @Override
    public void update(ClientId clientId, List<Integer> fileIds) {
        try {
            logger.info("Making update request...");
            trackerProtocol.writeUpdateRequest(new UpdateRequest(clientId.getPort(), fileIds));
            UpdateResponse r = trackerProtocol.readUpdateResponse();
            logger.info("Got response: " + r.toString());
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
            logger.info("Making upload request...");
            trackerProtocol.writeUploadRequest(new UploadRequest(fileInfo.getName(), fileInfo.getSize()));
            UploadResponse r = trackerProtocol.readUploadResponse();
            logger.info("Got response: " + r.toString());
            return r.getFileId();
        } catch (IOException e) {
            throw new RemoteTrackerError("upload", e);
        }
    }

    @Override
    public List<ClientId> source(Integer fileId) {
        try {
            logger.info("Making sources request...");
            trackerProtocol.writeSourcesRequest(new SourcesRequest(fileId));
            SourcesResponse r = trackerProtocol.readSourcesResponse();
            logger.info("Got response: " + r.toString());
            return r.getClients();
        } catch (IOException e) {
            throw new RemoteTrackerError("sources", e);
        }
    }

    @Override
    public void removeClient(ClientId clientId) {
        // todo: send update with empty list to server
        throw new UnsupportedOperationException("remove client on remote tracker");
    }
}
