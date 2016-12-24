package ru.spbau.mit.java;


import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.leech.FileBlocksDownloader;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;
import ru.spbau.mit.java.tracker.ThreadSafeIntIdProducer;
import ru.spbau.mit.java.tracker.ThreadSafeTracker;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MultiClientTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private Tracker<ClientId, Integer> tracker;
    private TrackerServer trackerServer;
    private List<TrackerClient> trackerClients = new ArrayList<>();
    private List<String> clientsFileForUpload = new ArrayList<>();
    private final int clientNumber = 100;
    private long updatePeriodLong = TimeUnit.MINUTES.toMillis(5);
    private final int blockSize = 100;

    @Before
    public void setup() throws IOException {
        tracker = new ThreadSafeTracker<>(new ThreadSafeIntIdProducer(0));
        trackerServer = new TrackerServer(
                8081, tracker, updatePeriodLong
        );

        trackerServer.start();

        for (int i = 0; i < clientNumber; ++i) {
            clientsFileForUpload.add(TestUtil.createFile(tmp, blockSize * 10));
        }

        for (int i = 0; i < clientNumber; ++i) {
            Socket serverConnection = new Socket("localhost", 8081);
            TrackerClient tc = TestUtil.createClient(serverConnection, 5678 + i, updatePeriodLong, blockSize);
            trackerClients.add(tc);
            tc.startSeedingServerThread();
            tc.startTrackerPeriodicUpdater();
        }
    }

    @After
    public void destroy() throws InterruptedException {
        for (TrackerClient tc : trackerClients) {
            tc.stopSeedingServerThread();
            tc.stopTrackerPeriodicUpdater();
        }
        trackerServer.stop();
        Thread.sleep(100); // sometimes socket do not have time to become free (or I'am mad)
    }


    @Test
    public void testUploadDownload() throws IOException, InterruptedException {
        Map<String, String> trackerFileNameToLocPath = new HashMap<>();
        // uploading files
        for (int i = 0; i < clientNumber; ++i) {
            String trackerFileName = "file_" + i;
            trackerClients.get(i).uploadFile(clientsFileForUpload.get(i), "file_" + i);
            trackerFileNameToLocPath.put(trackerFileName, clientsFileForUpload.get(i));
        }

        List<TrackerFile<Integer>> fileList = tracker.list();
        Assert.assertEquals(clientNumber, fileList.size());

        Random random = new Random();
        List<String> downloadLocations = new ArrayList<>();
        List<String> downloadingTrackerFileNames = new ArrayList<>();
        List<FileBlocksDownloader> downloaders = new ArrayList<>();
        // starting download from all clients
        for (int i = 0; i < clientNumber; ++i) {
            int idx = random.nextInt(clientNumber);
            while (idx == i) {
                // downloading file, which is already in the storage is prohibited...
                idx = random.nextInt(clientNumber);
            }
            TrackerFile<Integer> file = fileList.get(idx);
            String pathTo = tmp.newFile().getAbsolutePath();
            downloadLocations.add(pathTo);
            downloadingTrackerFileNames.add(file.getName());
            FileBlocksDownloader downloader = trackerClients.get(i).getFileDownloader(file, pathTo);
            downloaders.add(downloader);
            downloader.start();
        }

        for (FileBlocksDownloader d : downloaders) {
            d.join();
        }

        destroy();

        // checking if downloaded file equal to local
        for (int i = 0; i < clientNumber; ++i) {
            boolean areEqual = TestUtil.compareFilesContents(
                    downloadLocations.get(i),
                    trackerFileNameToLocPath.get(downloadingTrackerFileNames.get(i))
            );
            Assert.assertTrue(areEqual);
        }
    }
}
