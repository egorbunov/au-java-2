package ru.spbau.mit.java.protocol;

import ru.spbau.mit.protocol.request.*;

public interface RequestProtocol {
    byte readRequestCode();

    UpdateRequest readUpdateRequest();

    UploadRequest readUploadRequest();

    SourcesRequest readSourcesRequest();

    ListRequest readListRequest();
}
