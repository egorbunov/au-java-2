package ru.spbau.mit.java;

import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.leech.SeederConnectionFactory;
import ru.spbau.mit.java.leech.SeederConnectionFactoryImpl;
import ru.spbau.mit.java.shared.protocol.ClientTrackerProtocolImpl;
import ru.spbau.mit.java.shared.tracker.ClientId;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TestUtil {
    static String createFile(TemporaryFolder tmp, int maxSize) throws IOException {
        File file = tmp.newFile();
        byte[] bytes = new byte[new Random().nextInt(maxSize)];
        new Random().nextBytes(bytes);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        out.write(bytes);
        out.flush();
        out.close();
        return file.getAbsolutePath();
    }

    static long getFileSize(String path) {
        try {
            return Files.size(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean compareFilesContents(String pathA, String pathB) throws IOException {
        byte[] bytesA = TestUtil.getFileBytes(pathA);
        byte[] bytesB = TestUtil.getFileBytes(pathB);
        if (!Arrays.equals(bytesA, bytesB)) {
            System.out.println("WHOA");
        }
        return Arrays.equals(bytesA, bytesB);
    }

    static byte[] getFileBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    static <T> boolean unorderedListCompare(List<? extends T> as, List<? extends T> bs) {
        if (as.size() != bs.size()) {
            return false;
        }
        Set<T> setA = as.stream().collect(Collectors.toSet());
        return setA.containsAll(bs);
    }

    static TrackerClient createClient(Socket serverConnection, int port, long updatePeriod, int blockSize)
            throws IOException {
        FileBlocksStorage blocksStorage = new SimpleBlockStorage(blockSize);
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
                serverConnection.getLocalAddress().getAddress(), seederConnectionFactory,
                updatePeriod
        );
    }
 }
