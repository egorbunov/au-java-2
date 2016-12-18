package ru.spbau.mit.java;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.java.shared.error.UnknownRequestCode;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocol;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
import ru.spbau.mit.java.shared.protocol.ServerTrackerProtocol;
import ru.spbau.mit.java.shared.protocol.ServerTrackerProtocolImpl;
import ru.spbau.mit.java.shared.request.*;
import ru.spbau.mit.java.shared.response.ListResponse;
import ru.spbau.mit.java.shared.response.SourcesResponse;
import ru.spbau.mit.java.shared.response.UpdateResponse;
import ru.spbau.mit.java.shared.response.UploadResponse;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientTrackerProtocolTest {
    private ClientTrackerProtocol clientProtocol;
    private ServerTrackerProtocol serverProtocol;

    @Before
    public void setup() throws IOException {
        PipedOutputStream requestOut = new PipedOutputStream();
        PipedOutputStream responseOut = new PipedOutputStream();
        InputStream requestIn = new PipedInputStream(requestOut);
        InputStream responseIn = new PipedInputStream(responseOut);
        clientProtocol = new ClientTrackerProtocolImpl(responseIn, requestOut);
        serverProtocol = new ServerTrackerProtocolImpl(requestIn, responseOut);
    }

    @Test
    public void testListRequest() throws IOException, UnknownRequestCode {
        ListRequest r = new ListRequest();
        clientProtocol.writeListRequest(r);
        RequestCode code = serverProtocol.readRequestCode();
        Assert.assertEquals(RequestCode.LIST, code);
        serverProtocol.readListRequest();
    }

    @Test
    public void testUpdateRequest() throws IOException, UnknownRequestCode {
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
    public void testUploadRequest() throws IOException, UnknownRequestCode {
        UploadRequest r = new UploadRequest("filename", 42);
        clientProtocol.writeUploadRequest(r);
        RequestCode code = serverProtocol.readRequestCode();
        Assert.assertEquals(RequestCode.UPLOAD, code);
        UploadRequest actual = serverProtocol.readUploadRequest();

        Assert.assertEquals(r.getName(), actual.getName());
        Assert.assertEquals(r.getSize(), actual.getSize());
    }

    @Test
    public void testSourcesRequest() throws IOException, UnknownRequestCode {
        SourcesRequest r = new SourcesRequest(42);
        clientProtocol.writeSourcesRequest(r);
        RequestCode code = serverProtocol.readRequestCode();
        Assert.assertEquals(RequestCode.SOURCES, code);
        SourcesRequest actual = serverProtocol.readSourcesRequest();
        Assert.assertEquals(r.getFileId(), actual.getFileId());
    }

    @Test
    public void testListResponse() throws IOException {
        ListResponse expectedResponse =
                new ListResponse(Collections.singletonList(new TrackerFile<>(1, "NAME", 1)));
        serverProtocol.writeListResponse(expectedResponse);
        ListResponse response = clientProtocol.readListResponse();

        Assert.assertTrue(response.getFiles().size() == 1);
        TrackerFile<Integer> tf = response.getFiles().get(0);

        Assert.assertEquals("NAME", tf.getName());
        Assert.assertEquals(1, (long) tf.getId());
        Assert.assertEquals(1, tf.getSize());
    }

    @Test
    public void testUpdateResponse() throws IOException {
        UpdateResponse expectedResponse = new UpdateResponse(true);
        serverProtocol.writeUpdateResponse(expectedResponse);
        Assert.assertTrue(clientProtocol.readUpdateResponse().getStatus());
    }

    @Test
    public void testUploadResponse() throws IOException {
        UploadResponse expectedResponse = new UploadResponse(42);
        serverProtocol.writeUploadResponse(expectedResponse);
        UploadResponse actualResponse = clientProtocol.readUploadResponse();
        Assert.assertEquals(42, actualResponse.getFileId());
    }

    @Test
    public void testSourcesResponse() throws IOException {
        ClientId id = new ClientId(
                new byte[]{1,2,3,4},
                (short) 42
        );
        SourcesResponse expectedResponse = new SourcesResponse(Collections.singletonList(id));

        serverProtocol.writeSourcesResponse(expectedResponse);
        SourcesResponse actualResponse = clientProtocol.readSourcesResponse();

        Assert.assertTrue(actualResponse.getClients().size() == 1);
        Assert.assertEquals(id, actualResponse.getClients().get(0));
    }
}
