package ru.spbau.mit.java;


import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.leech.SeederConnectionFactory;
import ru.spbau.mit.java.leech.SeederConnectionFactoryImpl;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.tracker.ThreadSafeIntIdProducer;
import ru.spbau.mit.java.tracker.ThreadSafeTracker;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class TwoClientsTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private Tracker<ClientId, Integer> tracker;
    private TrackerServer trackerServer;
    private TrackerClient clientOne;
    private TrackerClient clientTwo;

    private TrackerClient createClient(Socket serverConnection, int port) throws IOException {
        FileBlocksStorage blocksStorage = new SimpleBlockStorage();
        RemoteTracker remoteTracker = new RemoteTracker(
                new ClientTrackerProtocolImpl(
                        serverConnection.getInputStream(),
                        serverConnection.getOutputStream()
                )
        );
        SeederConnectionFactory<ClientId> seederConnectionFactory
                = new SeederConnectionFactoryImpl(blocksStorage.getBlockSize());

        return new TrackerClient(
                blocksStorage,
                remoteTracker,
                port,
                seederConnectionFactory,
                serverConnection.getLocalAddress().getAddress()
        );
    }

    @Before
    public void setup() throws IOException {
        tracker = new ThreadSafeTracker<>(new ThreadSafeIntIdProducer(0));
        trackerServer = new TrackerServer(8081, tracker);

        trackerServer.start();

        Socket clientOneConnection = new Socket("localhost", 8081);
        Socket clientTwoConnection = new Socket("localhost", 8081);

        clientOne = createClient(clientOneConnection, 5678);
        clientTwo = createClient(clientTwoConnection, 5679);

        clientOne.startSeedingServerThread();
        clientOne.startTrackerPeriodicUpdater();

        clientTwo.startSeedingServerThread();
        clientTwo.startTrackerPeriodicUpdater();
    }

    @After
    public void destroy() {
        trackerServer.stop();
    }

    @Test
    public void test() throws IOException {
        File file = tmp.newFile("client_one_file1");
        clientOne.uploadFile(file.getAbsolutePath(), "client_one_file1");
    }

}
