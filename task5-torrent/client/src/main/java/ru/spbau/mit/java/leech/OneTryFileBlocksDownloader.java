package ru.spbau.mit.java.leech;


import ru.spbau.mit.java.files.FileBlocksStorage;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * One-try file downloader
 *
 * This file downloader makes only one try to start the file.
 * It knows fixed list of seeding clients, so it queries them for available
 * file blocks and starts downloading them in separate threads.
 *
 * In case of any seeder error (seeder may disconnect, for example) it does nothing.
 * So it will just start some blocks and then exit.
 *
 * @param <T> client id type
 */
public class OneTryFileBlocksDownloader<T> implements FileBlocksDownloader {
    private final Logger logger;
    private final int fileId;
    private int fileSize;
    private String destinationPath;
    private FileBlocksStorage fileBlocksStorage;
    private Set<T> seederIds;
    private final SeederConnectionFactory<T> seederConnectionFactory;
    private final List<Thread> downloadingThreads = new ArrayList<>();

    /**
     *
     * @param fileId id of the file to start
     * @param fileSize size of the file to start
     * @param destinationPath path, where file will be downloaded
     * @param fileBlocksStorage block storage, with which help file is written to disk
     * @param seederIds list of seeders ids
     * @param seederConnectionFactory factory of connections to seeders
     */
    public OneTryFileBlocksDownloader(int fileId,
                                      int fileSize,
                                      String destinationPath,
                                      FileBlocksStorage fileBlocksStorage,
                                      Set<T> seederIds,
                                      SeederConnectionFactory<T> seederConnectionFactory) {

        this.fileId = fileId;
        this.fileSize = fileSize;
        this.destinationPath = destinationPath;
        this.fileBlocksStorage = fileBlocksStorage;
        this.seederIds = seederIds;
        this.seederConnectionFactory = seederConnectionFactory;
        logger = Logger.getLogger("FileDownloader_" + fileId);
    }

    @Override
    public void start() throws IOException {
        if (!fileBlocksStorage.isFileInStorage(fileId)) {
            fileBlocksStorage.createEmptyFile(fileId, destinationPath, fileSize);
        }
        HashSet<Integer> alreadyHandledBlocks = new HashSet<>(fileBlocksStorage.getAvailableFileBlocks(fileId));
        HashMap<T, LinkedList<Integer>> clientBlocks = new HashMap<>();
        HashMap<T, SeederConnection> clientConnections = new HashMap<>();
        for (T client : seederIds) {
            SeederConnection conn = seederConnectionFactory.connectToClient(client);
            clientConnections.put(client, conn);
            clientBlocks.put(client, new LinkedList<>(conn.stat(fileId)));
        }
        HashMap<T, List<Integer>> finalClientBlockQueues =
                distributeBlocksAmongClients(alreadyHandledBlocks, clientBlocks);
        // closing not needed client connections
        for (T client : seederIds) {
            if (finalClientBlockQueues.get(client) == null) {
                clientConnections.get(client).disconnect();
            }
        }
        // starting downloading threads
        for (T client : seederIds) {
            if (finalClientBlockQueues.get(client) == null) {
                continue;
            }
            SeederConnection connection = clientConnections.get(client);
            logger.info("Starting downloading thread for client: " + client);
            OneClientDownloader downloader = new OneClientDownloader(connection, finalClientBlockQueues.get(client));
            Thread t = new Thread(downloader);
            downloadingThreads.add(t);
            t.start();
        }
    }

    @Override
    public void stop() {
        for (Thread t : downloadingThreads) {
            t.interrupt();
        }
    }

    @Override
    public void resume() {
        throw new UnsupportedOperationException("Can't resume " + this.getClass().getName());
    }

    @Override
    public void join() throws InterruptedException {
        for (Thread t : downloadingThreads) {
            t.join();
        }
    }

    @Override
    public int goalBlockNum() {
        return fileBlocksStorage.getTotalBlockNumber(fileId);
    }

    @Override
    public int downloadedBlockNum() {
        return fileBlocksStorage.getAvailableFileBlocksNumber(fileId);
    }

    /**
     * Distributes blocks fairly among clients...
     */
    private HashMap<T, List<Integer>> distributeBlocksAmongClients(HashSet<Integer> alreadyHandledBlocks,
                                                                   HashMap<T, LinkedList<Integer>> clientBlocks) {
        HashMap<T, List<Integer>> finalClientBlockQueues = new HashMap<>();
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
        return finalClientBlockQueues;
    }


    /**
     * Downloading routine for one client
     */
    private class OneClientDownloader implements Runnable {
        private final SeederConnection connection;
        private final Collection<Integer> blocks;

        OneClientDownloader(SeederConnection connection, Collection<Integer> blocks) {
            this.connection = connection;
            this.blocks = blocks;
        }

        @Override
        public void run() {
            logger.info("Going to start: " + blocks.size() + " blocks");
            for (Integer blockId : blocks) {
                if (Thread.interrupted()) {
                    logger.info("Downloader interrupted...");
                    break;
                }
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
