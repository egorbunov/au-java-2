package ru.spbau.mit.java.shared.protocol;

import ru.spbau.mit.java.shared.request.ListRequest;
import ru.spbau.mit.java.shared.request.SourcesRequest;
import ru.spbau.mit.java.shared.request.UpdateRequest;
import ru.spbau.mit.java.shared.request.UploadRequest;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;

import java.io.IOException;


public interface ClientTrackerProtocol {
    void writeUpdateRequest(UpdateRequest r) throws IOException;

    void writeUploadRequest(UploadRequest r) throws IOException;

    void writeListRequest(ListRequest r) throws IOException;

    void writeSourcesRequest(SourcesRequest r) throws IOException;

    UpdateResponse readUpdateResponse() throws IOException;

    UploadResponse readUploadResponse() throws IOException;

    ListResponse readListResponse() throws IOException;

    SourcesResponse readSourcesResponse() throws IOException;
}
