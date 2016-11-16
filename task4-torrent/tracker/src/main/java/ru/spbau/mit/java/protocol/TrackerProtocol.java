package ru.spbau.mit.java.protocol;


import ru.spbau.mit.java.shared.request.*;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;

import java.io.IOException;

public interface TrackerProtocol {
    RequestCode readRequestCode() throws IOException;

    UpdateRequest readUpdateRequest() throws IOException;
    UploadRequest readUploadRequest() throws IOException;
    SourcesRequest readSourcesRequest() throws IOException;
    ListRequest readListRequest();

    void writeUpdateResponse(UpdateResponse r) throws IOException;
    void writeUploadResponse(UploadResponse r) throws IOException;
    void writeListResponse(ListResponse r) throws IOException;
    void writeSourcesResponse(SourcesResponse r) throws IOException;
}