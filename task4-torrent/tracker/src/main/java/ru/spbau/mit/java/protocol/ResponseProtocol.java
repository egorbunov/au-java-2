package ru.spbau.mit.java.protocol;


import ru.spbau.mit.protocol.response.*;

public interface ResponseProtocol {

    void writeUpdateResponse(UpdateResponse response);

    void writeUploadResponse(UploadResponse response);

    void writeListResponse(ListResponse response);

    void writeSourcesResponse(SourcesResponse response);
}
