package ru.spbau.mit.java.leech;


import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.shared.tracker.Tracker;

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
    private final int fileId;
    private FileBlocksStorage fileBlocksStorage;
    private final Tracker<T, Integer> tracker;
    private final SeederConnectionFactory<T> seederConnectionFactory;

    public FileDownloader(int fileId,
                          FileBlocksStorage fileBlocksStorage,
                          Tracker<T, Integer> tracker,
                          SeederConnectionFactory<T> seederConnectionFactory) {

        this.fileId = fileId;
        this.fileBlocksStorage = fileBlocksStorage;
        this.tracker = tracker;
        this.seederConnectionFactory = seederConnectionFactory;
    }


}
