package ru.spbau.mit.java;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocol;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
import ru.spbau.mit.java.shared.protocol.ServerTrackerProtocol;
import ru.spbau.mit.java.shared.protocol.ServerTrackerProtocolImp;
import ru.spbau.mit.java.shared.request.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.List;

public class TrackerProtocolTest {
    private ClientTrackerProtocol clientProtocol;
    private ServerTrackerProtocol serverProtocol;

    @Before
    public void setup() throws IOException {
        PipedOutputStream requestOut = new PipedOutputStream();
        PipedOutputStream responseOut = new PipedOutputStream();
        InputStream requestIn = new PipedInputStream(requestOut);
        InputStream responseIn = new PipedInputStream(responseOut);

        clientProtocol = new ClientTrackerProtocolImpl(responseIn, requestOut);
        serverProtocol = new ServerTrackerProtocolImp(requestIn, responseOut);
    }

    @Test
    public void testListRequest() throws IOException {
        ListRequest r = new ListRequest();
        clientProtocol.writeListRequest(r);
        RequestCode code = serverProtocol.readRequestCode();
        Assert.assertEquals(RequestCode.LIST, code);
        serverProtocol.readListRequest();
    }

    @Test
    public void testUpdateRequest() throws IOException {
        List<Integer> fileIds = Arrays.asList(1, 2, 3);
        UpdateRequest r = new UpdateRequest((short) 42, fileIds);
        clientProtocol.writeUpdateRequest(r);
        RequestCode code = serverProtocol.readRequestCode();
        Assert.assertEquals(RequestCode.UPDATE, code);
        UpdateRequest actual = serverProtocol.readUpdateRequest();

        Assert.assertArrayEquals(r.getFileIds().toArray(new Integer[]{}),
                                 actual.getFileIds().toArray(new Integer[]{}));
        Assert.assertEquals(r.getClientPort(), actual.getClientPort());
    }

    @Test
    public void testUploadRequest() throws IOException {
        UploadRequest r = new UploadRequest("filename", 42);
        clientProtocol.writeUploadRequest(r);
        RequestCode code = serverProtocol.readRequestCode();
        Assert.assertEquals(RequestCode.UPLOAD, code);
        UploadRequest actual = serverProtocol.readUploadRequest();

        Assert.assertEquals(r.getName(), actual.getName());
        Assert.assertEquals(r.getSize(), actual.getSize());
    }

    @Test
    public void testSourcesRequest() throws IOException {
        SourcesRequest r = new SourcesRequest(42);
        clientProtocol.writeSourcesRequest(r);
        RequestCode code = serverProtocol.readRequestCode();
        Assert.assertEquals(RequestCode.SOURCES, code);
        SourcesRequest actual = serverProtocol.readSourcesRequest();
        Assert.assertEquals(r.getFileId(), actual.getFileId());
    }
}
