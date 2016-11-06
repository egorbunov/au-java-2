package ru.spbau.mit.java.tracker;

import ru.spbau.mit.protocol.tracker.TrackerFile;

import java.util.Collection;
import java.util.List;

/**
 *
 * @param <U> client identifier
 * @param <F> file identifier
 */
public interface Tracker<U, F> {
    /**
     * Finds files, which are currently available.
     * Available file is file, which was passed as
     * parameter to {@code update()} method
     */
    Collection<TrackerFile<F>> list();

    /**
     * Update list of files, which are seeded by specified seed
     * @param clientId seed id
     * @param fileIds file ids
     */
    void update(U clientId, List<F> fileIds);

    /**
     * Add new file for tracking
     * Clients can call update function with id returned
     * by this method to make file available for further
     * downloading
     * @param fileInfo file description
     * @return unique file identifier
     */
    F upload(FileInfo fileInfo);

    /**
     * Find all seeds of given file
     * @param fileId file identifier
     * @return collection of seeds (clients) ids
     */
    Collection<U> source(F fileId);


    /**
     * Tell tracker, that client have disconnected
     */
    void removeClient(U clientId);
}
