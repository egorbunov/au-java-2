package ru.spbau.mit.java.leech;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.shared.tracker.Tracker;

import java.io.IOError;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * This downloader uses {@link OneTryFileBlocksDownloader} many times in a row
 * to ensure that all of the file blocks are downloaded...
 */
public class FullFileDownloader<T> implements FileBlocksDownloader {
    private final Logger logger = Logger.getLogger(FullFileDownloader.class.getName());
    private final int fileId;
    private final int fileSize;
    private final String destinationPath;
    private final FileBlocksStorage fileBlocksStorage;
    private final Tracker<T, Integer> tracker;
    private final SeederConnectionFactory<T> seederConnectionFactory;
    private Thread downloadingThread = null;
    private volatile OneTryFileBlocksDownloader<T> curTryDownloader = null;
    private FileDownloadingStatus status;

    /**
     * @param fileId                  id of the file to start
     * @param fileSize                size of the file to start
     * @param destinationPath         target path where to start file
     * @param fileBlocksStorage       blocks storage with which file blocks are written to disk...
     * @param tracker                 tracker to get available seeder ids
     * @param seederConnectionFactory factory to produce connection to seeds for downloading
     */
    public FullFileDownloader(int fileId,
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
        this.status = FileDownloadingStatus.NOT_STARTED;
    }

    @Override
    public void start() throws IOException {
        if (!fileBlocksStorage.isFileInStorage(fileId)) {
            fileBlocksStorage.createEmptyFile(fileId, destinationPath, fileSize);
        }
        resume();
    }

    @Override
    public void stop() {
        if (curTryDownloader != null) {
            curTryDownloader.stop();
        }
        downloadingThread.interrupt();
        downloadingThread = null;
    }

    @Override
    public void resume() {
        if (downloadingThread != null && downloadingThread.isAlive()) {
            return;
        }

        downloadingThread = new Thread(new DownloadingTask());
        downloadingThread.start();
    }

    @Override
    public void join() throws InterruptedException {
        downloadingThread.join();
    }

    @Override
    public int goalBlockNum() {
        return fileBlocksStorage.getTotalBlockNumber(fileId);
    }

    @Override
    public int downloadedBlockNum() {
        return fileBlocksStorage.getAvailableFileBlocksNumber(fileId);
    }

    private class DownloadingTask implements Runnable {
        @Override
        public void run() {
            logger.info("Staring downloading...");
            FullFileDownloader.this.status = FileDownloadingStatus.IN_PROGRESS;
            while (!Thread.interrupted()) {
                if (fileBlocksStorage.getAvailableFileBlocksNumber(fileId) ==
                        fileBlocksStorage.getTotalBlockNumber(fileId)) {
                    logger.info("All blocks downloaded...");
                    FullFileDownloader.this.status = FileDownloadingStatus.FINISHED;
                    break;
                }

                curTryDownloader = new OneTryFileBlocksDownloader<>(
                        fileId,
                        fileSize,
                        destinationPath,
                        fileBlocksStorage,
                        new HashSet<>(tracker.source(fileId)),
                        seederConnectionFactory
                );

                try {
                    curTryDownloader.start();
                    curTryDownloader.join();
                } catch (IOException e) {
                    throw new IOError(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
