package ru.spbau.mit.java.leech;


import java.util.Collection;

/**
 * One seeder connection
 */
public interface SeederConnection {
    /**
     * Returns collection of available file parts on seeder side of
     * the connection.
     *
     * @param fileId id of the file to stat
     * @return collection of file parts
     * @throws ru.spbau.mit.java.files.error.FileNotExists in case of no file with
     *         id specified available on the seeder side
     */
    Collection<Integer> stat(int fileId);

    /**
     * Download one file part (block) from client
     * @param fileId file identifier
     * @param blockId block in file id
     * @return part in bytes in case of success
     * @throws ru.spbau.mit.java.files.error.BlockNotPresent in case block was not found
     *         on the seeder side
     * @throws ru.spbau.mit.java.files.error.FileNotExists in case no such file is held
     *         by seeder
     */
    byte[] downloadFileBlock(int fileId, int blockId);
}

