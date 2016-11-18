package ru.spbau.mit.java;


import ru.spbau.mit.java.protocol.TrackerProtocol;
import ru.spbau.mit.java.shared.RequestServer;
import ru.spbau.mit.java.shared.error.ServeIOError;
import ru.spbau.mit.java.shared.request.*;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;

import java.io.IOException;

/**
 * This is request serving class. It uses request executor
 * injected as dependency and it also ensures, that every request
 * gets it response in case of no exceptions occurs
 * (so it acts like part protocol a little bit)
 */
public class TrackerRequestServer implements RequestServer {
    private final TrackerProtocol trackerProtocol;
    private final TrackerRequestExecutor requestExecutor;

    /**
     *
     * @param trackerProtocol rules for writing/reading requests/responses
     * @param requestExecutor request executing logic
     */
    public TrackerRequestServer(TrackerProtocol trackerProtocol,
                                TrackerRequestExecutor requestExecutor) {

        this.trackerProtocol = trackerProtocol;
        this.requestExecutor = requestExecutor;
    }

    @Override
    public void serveOneRequest() {
        try {
            RequestCode requestCode = trackerProtocol.readRequestCode();
            switch (requestCode) {
                case UPDATE: {
                    UpdateRequest r = trackerProtocol.readUpdateRequest();
                    UpdateResponse response = requestExecutor.executeUpdate(r);
                    trackerProtocol.writeUpdateResponse(response);
                    break;
                }
                case UPLOAD: {
                    UploadRequest r = trackerProtocol.readUploadRequest();
                    UploadResponse response = requestExecutor.executeUpload(r);
                    trackerProtocol.writeUploadResponse(response);
                    break;
                }
                case SOURCES: {
                    SourcesRequest r = trackerProtocol.readSourcesRequest();
                    SourcesResponse response = requestExecutor.executeSource(r);
                    trackerProtocol.writeSourcesResponse(response);
                    break;
                }
                case LIST: {
                    ListRequest r = trackerProtocol.readListRequest();
                    ListResponse response = requestExecutor.executeList(r);
                    trackerProtocol.writeListResponse(response);
                    break;
                }
            }
        } catch (IOException e) {
            throw new ServeIOError(e.getCause());
        }
    }
}
