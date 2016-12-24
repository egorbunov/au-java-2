package ru.spbau.mit.java;

import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;
import ru.spbau.mit.java.shared.request.ListRequest;
import ru.spbau.mit.java.shared.request.SourcesRequest;
import ru.spbau.mit.java.shared.request.UpdateRequest;
import ru.spbau.mit.java.shared.request.UploadRequest;
import ru.spbau.mit.java.shared.response.UpdateResponse;

/**
 * Interface tends to encapsulate request processing logic, so user
 * can implement many request processing scenarios regardless of reading
 * requests and writing responses for them
 */
public interface TrackerRequestExecutor {
    UpdateResponse executeUpdate(UpdateRequest r);

    UploadResponse executeUpload(UploadRequest r);

    SourcesResponse executeSource(SourcesRequest r);

    ListResponse executeList(ListRequest r);
}
