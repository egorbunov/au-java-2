package ru.spbau.mit.java;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.java.protocol.LeechProtocol;
import ru.spbau.mit.java.protocol.LeechProtocolImpl;
import ru.spbau.mit.java.protocol.SeedProtocol;
import ru.spbau.mit.java.protocol.SeedProtocolImpl;
import ru.spbau.mit.java.protocol.request.ClientRequestCode;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;

import java.io.*;

public class LeechSeedProtocolsTest {
    private LeechProtocol leechProtocol;
    private SeedProtocol seedProtocol;

    @Before
    public void setup() throws IOException {
        PipedOutputStream requestOut = new PipedOutputStream();
        PipedOutputStream responseOut = new PipedOutputStream();
        InputStream requestIn = new PipedInputStream(requestOut);
        InputStream responseIn = new PipedInputStream(responseOut);

        leechProtocol = new LeechProtocolImpl(responseIn, requestOut, 100);
        seedProtocol = new SeedProtocolImpl(requestIn, responseOut);
    }


    @Test
    public void testStatRequest() throws IOException {
        StatRequest expectedRequest = new StatRequest(42);
        leechProtocol.writeStatRequest(expectedRequest);
        ClientRequestCode code = seedProtocol.readRequestCode();
        Assert.assertEquals(code, ClientRequestCode.STAT);
        StatRequest actual = seedProtocol.readStatRequest();
        Assert.assertEquals(expectedRequest.getFileId(), actual.getFileId());
    }

    @Test
    public void testGetRequest() throws IOException {
        GetPartRequest expectedRequest = new GetPartRequest(42, 43);
        leechProtocol.writeGetPartRequest(expectedRequest);
        ClientRequestCode code = seedProtocol.readRequestCode();
        Assert.assertEquals(code, ClientRequestCode.GET);
        GetPartRequest actual = seedProtocol.readGetPartRequest();

        Assert.assertEquals(expectedRequest.getFileId(), actual.getFileId());
        Assert.assertEquals(expectedRequest.getPartId(), actual.getPartId());
    }
}
