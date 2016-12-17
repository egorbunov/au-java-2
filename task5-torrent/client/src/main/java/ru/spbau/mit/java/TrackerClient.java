package ru.spbau.mit.java;

import ru.spbau.mit.java.error.FileAlreadyDownloaded;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.error.FileNotExistsInStorage;
import ru.spbau.mit.java.leech.FileBlocksDownloader;
import ru.spbau.mit.java.leech.FullFileDownloader;
import ru.spbau.mit.java.leech.SeederConnectionFactory;
import ru.spbau.mit.java.seed.SeedingServer;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class which aggregates all the client work together:
 *  1. Starts seeding server to listen to other client (leechers) connections
 *  2. Starts thread for periodically updating it's info to the tracker
 *  3. Provides interface for file downloading and stuff
 */
public class TrackerClient {
    private Logger logger = Logger.getLogger(TrackerClient.class.getSimpleName());
    private final ClientId seederId;
    private final FileBlocksStorage blocksStorage;
    private final Tracker<ClientId, Integer> remoteTracker;
    private int seedingPort;
    private final SeederConnectionFactory<ClientId> seederConnectionFactory;
    private final long updateRequestPeriod;
    private SeedingServer seedingServer;
    private Thread trackerUpdater;

    /**
     * @param blocksStorage storage, where all downloaded / uploaded files are stored
     * @param remoteTracker tracker model, which will be queried for seeded files information
     *                      and clients, which are seeding this files
     * @param seedingPort port, where to listen requests for file downloads from this client
     * @param clientIp ip of this client
     * @param seederConnectionFactory factory, which creates connections to other clients as seeders,
*                                it is used to start file to current client from other "remote"
     */
    public TrackerClient(FileBlocksStorage blocksStorage,
                         Tracker<ClientId, Integer> remoteTracker,
                         int seedingPort,
                         byte[] clientIp,
                         SeederConnectionFactory<ClientId> seederConnectionFactory,
                         long updateRequestPeriod) {
        this.blocksStorage = blocksStorage;
        this.remoteTracker = remoteTracker;
        this.seedingPort = seedingPort;
        this.seederConnectionFactory = seederConnectionFactory;
        this.seederId = new ClientId(clientIp, (short) seedingPort);
        this.updateRequestPeriod = updateRequestPeriod;
    }

    /**
     * Starts thread, which waits for other client connections
     * and serves their requests
     */
    public void startSeedingServerThread() {
        seedingServer = new SeedingServer(seedingPort, blocksStorage);
        seedingServer.start();
    }

    public ClientId getSeederId() {
        return seederId;
    }

    /**
     * Stops seeding thread
     */
    public void stopSeedingServerThread() {
        seedingServer.stop();
    }

    /**
     * Starts thread, which periodically sends update request
     * to tracker designating it is still alive
     */
    public void startTrackerPeriodicUpdater() {
        trackerUpdater = new Thread(() -> {
            while (Thread.interrupted()) {
                remoteTracker.update(seederId, blocksStorage.getAvailableFileIds());
                try {
                    Thread.sleep(this.updateRequestPeriod);
                } catch (InterruptedException e) {
                    logger.info("Interrupted");
                }
            }
        });
        trackerUpdater.start();
    }

    public void stopTrackerPeriodicUpdater() {
        trackerUpdater.interrupt();
    }

    /**
     * queries tracker for available file list
     */
    public List<TrackerFile<Integer>> queryFileList() {
        return remoteTracker.list();
    }

    /**
     * @param pathToFile local path
     * @param filenameOnTracker name of file on tracker
     */
    public int uploadFile(String pathToFile, String filenameOnTracker) throws IOException {
        long size = Files.size(Paths.get(pathToFile));
        Integer fileId = remoteTracker.upload(new FileInfo((int) size, filenameOnTracker));
        blocksStorage.addLocalFile(fileId, pathToFile);
        remoteTracker.update(seederId, blocksStorage.getAvailableFileIds());
        return fileId;
    }


    /**
     * Constructs downloader object for further file downloading control
     *
     * @param file file to download
     * @param destinationPath destination path to store downloaded file
     * @return {@link FileBlocksDownloader} object, with which help user can start downloading asynchronously
     *         join it ot pause it...
     * @throws IOException
     * @throws InterruptedException
     */
    public FileBlocksDownloader getFileDownloader(TrackerFile<Integer> file, String destinationPath)
            throws IOException, InterruptedException {

        if (blocksStorage.isFileFullyAvailable(file.getId())) {
            throw new FileAlreadyDownloaded(file, blocksStorage.getLocalFilePath(file.getId()));
        }

        FileBlocksDownloader fileDownloader = new FullFileDownloader<>(
                file.getId(),
                file.getSize(),
                destinationPath,
                blocksStorage,
                remoteTracker,
                seederConnectionFactory
        );

        ArrayList<Integer> newFileIds = new ArrayList<>(blocksStorage.getAvailableFileIds());
        newFileIds.add(file.getId());
        remoteTracker.update(seederId, newFileIds);

        return fileDownloader;
    }
}
