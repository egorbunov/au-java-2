package ru.spbau.mit.java;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.error.FileAlreadyDownloaded;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.leech.SeederConnectionFactory;
import ru.spbau.mit.java.leech.SeederConnectionFactoryImpl;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;
import ru.spbau.mit.java.tracker.ThreadSafeIntIdProducer;
import ru.spbau.mit.java.tracker.ThreadSafeTracker;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Egor Gorbunov
 * Date: 12/4/16
 * Email: egor-mailbox@ya.com
 */
public class OneClientTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private Tracker<ClientId, Integer> tracker;
    private TrackerServer trackerServer;
    private TrackerClient client;
    private TrackerClient clientTwo;
    private long updatePeriodShort = TimeUnit.MILLISECONDS.toMillis(200);
    private final int blockSize = 10;

    @Before
    public void setup() throws IOException {
        tracker = new ThreadSafeTracker<>(new ThreadSafeIntIdProducer(0));
        trackerServer = new TrackerServer(
                8081, tracker, updatePeriodShort
        );
        trackerServer.start();
        Socket clientOneConnection = new Socket("localhost", 8081);
        client = TestUtil.createClient(clientOneConnection, 5678, updatePeriodShort, blockSize);
        client.startSeedingServerThread();
        client.startTrackerPeriodicUpdater();
    }

    @After
    public void destroy() {
        trackerServer.stop();
        client.stopSeedingServerThread();
        client.stopTrackerPeriodicUpdater();
    }

    @Test(expected = FileAlreadyDownloaded.class)
    public void testDownloadSameId() throws IOException, InterruptedException {
        client.uploadFile(TestUtil.createFile(tmp, 100), "asd");
        TrackerFile<Integer> tf = tracker.list().get(0);
        client.getFileDownloader(tf, "whatever");
    }

    @Test
    public void testClientUpdateTimeout() throws InterruptedException, IOException {
        client.uploadFile(TestUtil.createFile(tmp, 100), "asd");
        Assert.assertFalse(tracker.list().isEmpty());
        client.stopTrackerPeriodicUpdater();
        Thread.sleep(updatePeriodShort * 2);
        Assert.assertTrue(tracker.list().isEmpty());
    }
}
