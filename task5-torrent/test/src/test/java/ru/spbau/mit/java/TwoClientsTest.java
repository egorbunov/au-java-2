package ru.spbau.mit.java;


import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.files.error.FileNotExistsInStorage;
import ru.spbau.mit.java.leech.FileBlocksDownloader;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;
import ru.spbau.mit.java.tracker.ThreadSafeIntIdProducer;
import ru.spbau.mit.java.tracker.ThreadSafeTracker;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TwoClientsTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private Tracker<ClientId, Integer> tracker;
    private TrackerServer trackerServer;
    private TrackerClient clientOne;
    private TrackerClient clientTwo;
    private long updatePeriodLong = TimeUnit.MINUTES.toMillis(5);
    private long updatePeriodShort = TimeUnit.SECONDS.toMillis(5);
    private final int blockSize = 10;

    @Before
    public void setup() throws IOException {
        tracker = new ThreadSafeTracker<>(new ThreadSafeIntIdProducer(0));
        trackerServer = new TrackerServer(
                8081, tracker, updatePeriodLong
        );

        trackerServer.start();

        Socket clientOneConnection = new Socket("localhost", 8081);
        Socket clientTwoConnection = new Socket("localhost", 8081);

        clientOne = TestUtil.createClient(clientOneConnection, 5678, updatePeriodLong, blockSize);
        clientTwo = TestUtil.createClient(clientTwoConnection, 5679, updatePeriodLong, blockSize);

        clientOne.startSeedingServerThread();
        clientOne.startTrackerPeriodicUpdater();

        clientTwo.startSeedingServerThread();
        clientTwo.startTrackerPeriodicUpdater();
    }

    @After
    public void destroy() {
        trackerServer.stop();
        clientOne.stopSeedingServerThread();
        clientOne.stopTrackerPeriodicUpdater();
        clientTwo.stopSeedingServerThread();
        clientTwo.stopTrackerPeriodicUpdater();
    }

    @Test
    public void testClientSeesOneFileUpdate() throws IOException {
        File file = tmp.newFile("client_one_file1");
        clientOne.uploadFile(file.getAbsolutePath(), "client_one_file1");
        List<TrackerFile<Integer>> trackerFiles = clientTwo.queryFileList();

        Assert.assertEquals(1, trackerFiles.size());
        TrackerFile<Integer> trackerFile = trackerFiles.get(0);
        Assert.assertEquals("client_one_file1", trackerFile.getName());
    }

    @Test
    public void testClientSeesManyFilesUpdate() throws IOException {
        String[] fileNames = new String[]{"1_file", "2_file", "3_file", "4_file"};
        List<String> paths = new ArrayList<>();
        for (String s : fileNames) {
            paths.add(TestUtil.createFile(tmp, blockSize * 100));
        }
        for (int i = 0; i < paths.size(); i++) {
            clientOne.uploadFile(paths.get(i), fileNames[i]);
        }
        List<TrackerFile<Integer>> trackerFiles = clientTwo.queryFileList();
        Assert.assertTrue(TestUtil.unorderedListCompare(
                Arrays.stream(fileNames).collect(Collectors.toList()),
                trackerFiles.stream().map(TrackerFile::getName).collect(Collectors.toList())
        ));

        Map<String, Integer> actualSizes = trackerFiles.stream()
                .collect(Collectors.toMap(TrackerFile::getName, TrackerFile::getSize));

        for (int i = 0; i < paths.size(); i++) {
            Assert.assertEquals(TestUtil.getFileSize(paths.get(i)), (long) actualSizes.get(fileNames[i]));
        }
    }

    @Test
    public void testSourcesRequest() throws IOException {
        String path = TestUtil.createFile(tmp, blockSize * 100);
        String fTrackerName = "file1";
        clientOne.uploadFile(path, fTrackerName);
        TrackerFile<Integer> trackerFile = clientTwo.queryFileList().get(0);

        List<ClientId> sources = tracker.source(trackerFile.getId());

        Assert.assertTrue(sources.size() == 1);
        Assert.assertEquals(clientOne.getSeederId(), sources.get(0));
    }

    @Test
    public void testDownloadFile() throws IOException, InterruptedException {
        String path = TestUtil.createFile(tmp, blockSize * 100);
        String fTrackerName = "file1";
        clientOne.uploadFile(path, fTrackerName);

        TrackerFile<Integer> trackerFile = clientTwo.queryFileList().get(0);
        String destPath = tmp.newFile().getAbsolutePath();
        FileBlocksDownloader downloader = clientTwo.getFileDownloader(trackerFile, destPath);

        downloader.start();
        downloader.join();

        Assert.assertTrue(TestUtil.compareFilesContents(path, destPath));
    }
}
