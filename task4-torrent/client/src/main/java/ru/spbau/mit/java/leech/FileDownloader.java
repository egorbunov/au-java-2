package ru.spbau.mit.java.leech;


import ru.spbau.mit.java.SeederConnectionFactory;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.Tracker;

/**
 * One file downloader
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
