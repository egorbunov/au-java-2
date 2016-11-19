package ru.spbau.mit.java.leech;


import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.shared.tracker.Tracker;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * One file downloader
 *
 * File downloader gets information about particular file
 * from tracker about seeders and them establishes connections
 * with this available clients to download file parts
 *
 * @param <T> client id type
 */
public class FileDownloader<T> {
    private final Logger logger;
    private final int fileId;
    private int fileSize;
    private String destinationPath;
    private FileBlocksStorage fileBlocksStorage;
    private final Tracker<T, Integer> tracker;
    private final SeederConnectionFactory<T> seederConnectionFactory;

    public FileDownloader(int fileId,
                          int fileSize,
                          String destinationPath,
                          FileBlocksStorage fileBlocksStorage,
                          Tracker<T, Integer> tracker,
                          SeederConnectionFactory<T> seederConnectionFactory) {

        this.fileId = fileId;
        this.fileSize = fileSize;
        this.destinationPath = destinationPath;
        this.fileBlocksStorage = fileBlocksStorage;
        this.tracker = tracker;
        this.seederConnectionFactory = seederConnectionFactory;
        logger = Logger.getLogger("FileDownloader_" + fileId);
    }

    public void download() throws IOException {
        Collection<T> clients = new HashSet<T>(tracker.source(fileId));

        if (!fileBlocksStorage.isFileInStorage(fileId)) {
            fileBlocksStorage.createEmptyFile(fileId, destinationPath, fileSize);
        }

        HashSet<Integer> alreadyHandledBlocks = new HashSet<>(fileBlocksStorage.getAvailableFileBlocks(fileId));
        HashMap<T, LinkedList<Integer>> clientBlocks = new HashMap<>();
        HashMap<T, SeederConnection> clientConnections = new HashMap<>();
        HashMap<T, List<Integer>> finalClientBlockQueues = new HashMap<>();



        for (T client : clients) {
            SeederConnection conn = seederConnectionFactory.connectToClient(client);
            clientConnections.put(client, conn);
            clientBlocks.put(client, new LinkedList<>(conn.stat(fileId)));
        }

        // distributing blocks among clients
        boolean updated = true;
        while (updated) {
            updated = false;
            // choosing blocks one by one from clients looping firstly through clients,
            // and not through all blocks in one client, so in the ideal case every client
            // will get equal amount of blocks to be queried
            for (Map.Entry<T, LinkedList<Integer>> e : clientBlocks.entrySet()) {
                LinkedList<Integer> blocks = e.getValue();
                while (!blocks.isEmpty()) {
                    Integer blockId = blocks.pollFirst(); // delete and return block id
                    if (alreadyHandledBlocks.contains(blockId)) {
                        // block is already distributed / downloaded
                        continue;
                    }
                    // block is not distributed, so add it to current client and
                    // switch to next client
                    updated = true;
                    alreadyHandledBlocks.add(blockId);
                    if (!finalClientBlockQueues.containsKey(e.getKey())) {
                        finalClientBlockQueues.put(e.getKey(), new ArrayList<>());
                    }
                    finalClientBlockQueues.get(e.getKey()).add(blockId);
                    break; // switch to next client
                }
            }
        }

        // closing not needed client connections
        for (T client : clients) {
            if (finalClientBlockQueues.get(client) == null) {
                clientConnections.get(client).disconnect();
            }
        }

        // starting downloading threads
        for (T client : clients) {
            if (finalClientBlockQueues.get(client) == null) {
                continue;
            }
            SeederConnection connection = clientConnections.get(client);
            logger.info("Starting downloading thread for client: " + client);
            new Thread(new OneClientDownloader(connection, finalClientBlockQueues.get(client))).start();
        }
    }

    /**
     * Downloading routine for one client
     */
    private class OneClientDownloader implements Runnable {
        private final SeederConnection connection;
        private final Collection<Integer> blocks;

        public OneClientDownloader(SeederConnection connection, Collection<Integer> blocks) {

            this.connection = connection;
            this.blocks = blocks;
        }

        @Override
        public void run() {
            for (Integer blockId : blocks) {
                try {
                    logger.info("Downloading one block: " + blockId);
                    byte[] bs = connection.downloadFileBlock(fileId, blockId);
                    fileBlocksStorage.writeFileBlock(fileId, blockId, bs);
                } catch (IOException e) {
                    throw new RuntimeException("error downloading block", e);
                }
            }
            try {
                connection.disconnect();
            } catch (IOException e) {
                throw new RuntimeException("error disconnecting =(");
            }
        }
    }
}
