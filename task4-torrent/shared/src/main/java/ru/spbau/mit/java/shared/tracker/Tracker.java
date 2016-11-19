package ru.spbau.mit.java.shared.tracker;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Interface for file tracker
 *
 * This interface is shared because on server (tracker) side it
 * is used to fill and query local structures holding all tracked file
 * info, but on client side this interface implementation may encapsulate
 * all communication with server and all that stuff
 *
 * @param <U> client identifier
 * @param <F> file identifier
 */
public interface Tracker<U, F> {
    /**
     * Finds files, which are currently available.
     * Available file is file, which was passed as
     * parameter to {@code executeUpdate()} method
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
     * Clients can call executeUpdate function with id returned
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
