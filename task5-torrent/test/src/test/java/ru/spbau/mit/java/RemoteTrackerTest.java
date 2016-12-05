package ru.spbau.mit.java;


import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;
import ru.spbau.mit.java.tracker.ThreadSafeIntIdProducer;
import ru.spbau.mit.java.tracker.ThreadSafeTracker;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemoteTrackerTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private Tracker<ClientId, Integer> serverTracker;
    private TrackerServer trackerServer;
    private RemoteTracker remoteTracker;
    private ClientId clientId;
    private Socket clientConnection;

    @Before
    public void setup() throws IOException {
        serverTracker = new ThreadSafeTracker<>(new ThreadSafeIntIdProducer(0));
        trackerServer = new TrackerServer(
                8081, serverTracker, TimeUnit.MINUTES.toMillis(5)
        );

        trackerServer.start();

        clientConnection = new Socket("localhost", 8081);

        FileBlocksStorage blocksStorage = new SimpleBlockStorage(10);
        remoteTracker = new RemoteTracker(
                new ClientTrackerProtocolImpl(
                        clientConnection.getInputStream(),
                        clientConnection.getOutputStream()
                )
        );
        clientId = new ClientId(clientConnection.getInetAddress().getAddress(), (short) 123);
    }

    @After
    public void destroy() throws InterruptedException {
        trackerServer.stop();
        Thread.sleep(100); // sometimes socket do not have time to become free (or I'am mad)
    }

    @Test
    public void testUploadUpdate() {
        Integer id = remoteTracker.upload(new FileInfo(10, "name"));
        remoteTracker.update(clientId, Collections.singletonList(id));

        List<TrackerFile<Integer>> files = serverTracker.list();
        Assert.assertTrue(files.size() == 1);
        TrackerFile<Integer> file = files.get(0);

        Assert.assertEquals("name", file.getName());
        Assert.assertEquals(10, file.getSize());
    }


    @Test
    public void testSources() {
        Integer id = remoteTracker.upload(new FileInfo(10, "name"));
        remoteTracker.update(clientId, Collections.singletonList(id));
        List<ClientId> source = remoteTracker.source(id);
        Assert.assertTrue(source.size() == 1);
        ClientId client = source.get(0);
        Assert.assertEquals(clientId, client);
    }

    @Test
    public void testList() {
        Integer id = remoteTracker.upload(new FileInfo(10, "name"));
        remoteTracker.update(clientId, Collections.singletonList(id));

        List<TrackerFile<Integer>> files = remoteTracker.list();
        Assert.assertTrue(files.size() == 1);
        TrackerFile<Integer> file = files.get(0);

        Assert.assertEquals("name", file.getName());
        Assert.assertEquals(10, file.getSize());
    }
}
