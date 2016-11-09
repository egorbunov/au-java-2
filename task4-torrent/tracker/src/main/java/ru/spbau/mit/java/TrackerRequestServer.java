package ru.spbau.mit.java;


import ru.spbau.mit.java.protocol.TrackerProtocol;
import ru.spbau.mit.java.shared.RequestServer;
import ru.spbau.mit.java.shared.ServeIOError;
import ru.spbau.mit.java.shared.request.*;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;

import java.io.IOException;

public class TrackerRequestServer implements RequestServer {
    private final TrackerProtocol trackerProtocol;
    private final TrackerRequestExecutor requestExecutor;

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
