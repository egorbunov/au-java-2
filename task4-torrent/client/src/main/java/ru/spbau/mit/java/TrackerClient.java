package ru.spbau.mit.java;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.leech.FileDownloader;
import ru.spbau.mit.java.leech.SeederConnectionFactory;
import ru.spbau.mit.java.seed.SeedingServer;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Class which aggregates all the client work together:
 *  1. Starts seeding server to listen to other client (leechers) connections
 *  2. Starts thread for periodically updating it's info to the tracker
 *  3. Provides interface for file downloading and stuff
 */
public class TrackerClient {
    private final ClientId seederId;
    private Logger logger = Logger.getLogger(TrackerClient.class.getSimpleName());
    private final FileBlocksStorage blocksStorage;
    private final RemoteTracker remoteTracker;
    private int seedingPort;
    private final SeederConnectionFactory<ClientId> seederConnectionFactory;
    private SeedingServer seedingServer;
    private Thread trackerUpdater;

    public Map<String, Integer> getSeedingFiles() {
        return seedingFiles;
    }

    public void setSeedingFiles(Map<String, Integer> seedingFiles) {
        this.seedingFiles = seedingFiles;
    }

    private Map<String, Integer> seedingFiles = new HashMap<>();

    public TrackerClient(FileBlocksStorage blocksStorage,
                         RemoteTracker remoteTracker,
                         int seedingPort,
                         SeederConnectionFactory<ClientId> seederConnectionFactory,
                         byte[] clientIp) {
        this.blocksStorage = blocksStorage;
        this.remoteTracker = remoteTracker;
        this.seedingPort = seedingPort;
        this.seederConnectionFactory = seederConnectionFactory;
        this.seederId = new ClientId(clientIp, (short) seedingPort);
    }

    /**
     * Starts thread, which waits for other client connections
     * and serves their requests
     */
    public void startSeedingServerThread() {
        seedingServer = new SeedingServer(seedingPort, blocksStorage);
        seedingServer.start();
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
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(5));
                } catch (InterruptedException e) {
                    logger.info("Interrupted");
                }
                remoteTracker.update(seederId,
                        new ArrayList<>(seedingFiles.values()));
            }
        });
        trackerUpdater.start();
    }

    public void stopTrackerPeriodicUpdater() {
        trackerUpdater.interrupt();
    }

    /**
     * queries tracker for available file list
     * TODO: mb exclude files, which are already downloaded by this client
     */
    public Collection<TrackerFile<Integer>> queryFileList() {
        return remoteTracker.list();
    }

    /**
     * @param pathToFile local path
     * @param filenameOnTracker name of file on tracker
     */
    public void uploadFile(String pathToFile, String filenameOnTracker) throws IOException {
        long size = Files.size(Paths.get(pathToFile));
        Integer fileId = remoteTracker.upload(new FileInfo((int) size, filenameOnTracker));
        blocksStorage.addLocalFile(fileId, pathToFile);

        seedingFiles.put(Paths.get(pathToFile).toAbsolutePath().toString(), fileId);

        remoteTracker.update(seederId, new ArrayList<>(seedingFiles.values()));
    }

    public void downloadFile(TrackerFile<Integer> file, String destinationPath) throws IOException, InterruptedException {
        FileDownloader<ClientId> fileDownloader = new FileDownloader<>(
                file.getId(),
                file.getSize(),
                destinationPath,
                blocksStorage,
                remoteTracker,
                seederConnectionFactory
        );

        logger.info("making file available for others before downloading...");
        seedingFiles.put(destinationPath, file.getId());
        remoteTracker.update(seederId, new ArrayList<>(seedingFiles.values()));

        logger.info("Downloading file " + file.getId() + "...");
        fileDownloader.download();
        logger.info("Downloaded!");

    }
}
