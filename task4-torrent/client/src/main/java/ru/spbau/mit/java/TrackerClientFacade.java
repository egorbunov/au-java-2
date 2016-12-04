package ru.spbau.mit.java;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.leech.FileBlocksDownloader;
import ru.spbau.mit.java.leech.SeederConnectionFactoryImpl;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Combines seed/leech part and remote tracker interface
 */
public class TrackerClientFacade {
    private final String trackerHost;
    private final int trackerPort;
    private FileBlocksStorage clientFileStorage;
    private int seedingPort;
    private Socket serverConnection;
    private Tracker<ClientId, Integer> remoteTracker;
    private TrackerClient trackerClient;

    /**
     * @param trackerHost host for remote tracker server
     * @param trackerPort port for remote tracker server
     * @param clientFileStorage block storage for this client,
     *                          from this storage it is possible
     *                          to determine which files can be
     *                          seeded
     * @param seedingPort port, where client runs it's seeding server
     */
    public TrackerClientFacade(String trackerHost,
                               int trackerPort,
                               FileBlocksStorage clientFileStorage,
                               int seedingPort) {

        this.trackerHost = trackerHost;
        this.trackerPort = trackerPort;
        this.clientFileStorage = clientFileStorage;
        this.seedingPort = seedingPort;
    }

    /**
     * Queries remote tracker for available files
     *
     * @return list of files available for downloading
     */
    List<TrackerFile<Integer>> listTrackerFiles() {
        return remoteTracker.list();
    }

    /**
     * Uploads file information to tracker and adds information about blocks to
     * block storage
     * @param pathToFile path to file to be added
     * @param filenameOnTracker name of the file, which will be seen by other clients
     * @return tracker file id
     */
    int uploadFile(String pathToFile, String filenameOnTracker) throws IOException {
        return trackerClient.uploadFile(pathToFile, filenameOnTracker);
    }

    /**
     * See description in {@link TrackerClient} method {@code getFileDownloader}
     */
    FileBlocksDownloader getFileDownloader(TrackerFile<Integer> file, String destinationPath)
            throws IOException, InterruptedException {
        return trackerClient.getFileDownloader(file, destinationPath);
    }

    /**
     * Connects to tracker server and after that starts all the client specific stuff:
     *  - task for periodic updates to server
     *  - seeding server
     */
    void start() throws IOException {
        Socket serverConnection = new Socket(trackerHost, trackerPort);
        remoteTracker = new RemoteTracker(
                new ClientTrackerProtocolImpl(
                        serverConnection.getInputStream(),
                        serverConnection.getOutputStream()
                )
        );
        trackerClient = new TrackerClient(
                clientFileStorage,
                remoteTracker,
                seedingPort,
                serverConnection.getLocalAddress().getAddress(),
                new SeederConnectionFactoryImpl(clientFileStorage.getBlockSize()),
                TimeUnit.MINUTES.toMillis(5)
        );

        trackerClient.startTrackerPeriodicUpdater();
        trackerClient.startSeedingServerThread();
    }

    /**
     * Close server connection and stop all other running tasks (seeding server and
     * periodic updater)
     */
    void stop() throws IOException {
        trackerClient.stopSeedingServerThread();
        trackerClient.stopTrackerPeriodicUpdater();
        serverConnection.close();
    }
}
