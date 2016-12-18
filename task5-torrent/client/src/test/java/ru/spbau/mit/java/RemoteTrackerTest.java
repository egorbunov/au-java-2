package ru.spbau.mit.java;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocol;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
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

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class RemoteTrackerTest {
    private RemoteTracker tracker;
    private ClientTrackerProtocol protocol;

    @Before
    public void setup() throws IOException {
        protocol = mock(ClientTrackerProtocolImpl.class);
        when(protocol.readListResponse()).thenReturn(new ListResponse(
                Collections.emptyList()
        ));
        when(protocol.readSourcesResponse()).thenReturn(new SourcesResponse(
                Collections.emptyList()
        ));
        when(protocol.readUploadResponse()).thenReturn(new UploadResponse(
                42
        ));
        when(protocol.readUpdateResponse()).thenReturn(new UpdateResponse(
                true
        ));
        tracker = new RemoteTracker(protocol);
    }

    @Test
    public void testList() throws IOException {
        tracker.list();
        InOrder inOrder = inOrder(protocol);
        inOrder.verify(protocol).writeListRequest(any());
        inOrder.verify(protocol).readListResponse();
    }

    @Test
    public void testUpload() throws IOException {
        FileInfo fInfo = new FileInfo(10, "name");
        tracker.upload(fInfo);
        InOrder inOrder = inOrder(protocol);
        inOrder.verify(protocol).writeUploadRequest(
                new UploadRequest(fInfo.getName(), fInfo.getSize())
        );
        inOrder.verify(protocol).readUploadResponse();
    }

    @Test
    public void testUpdate() throws IOException {
        tracker.update(new ClientId(new byte[]{}, (short) 42), Collections.emptyList());
        InOrder inOrder = inOrder(protocol);
        inOrder.verify(protocol).writeUpdateRequest(
                new UpdateRequest(
                        (short) 42, Collections.emptyList()
                )
        );
        inOrder.verify(protocol).readUpdateResponse();
    }

    @Test
    public void testSources() throws IOException {
        tracker.source(42);
        InOrder inOrder = inOrder(protocol);
        inOrder.verify(protocol).writeSourcesRequest(
                new SourcesRequest(42)
        );
        inOrder.verify(protocol).readSourcesResponse();
    }
}
