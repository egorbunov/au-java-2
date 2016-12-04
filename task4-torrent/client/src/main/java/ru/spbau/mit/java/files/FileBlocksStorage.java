package ru.spbau.mit.java.files;

import ru.spbau.mit.java.files.error.BadBlockSize;
import ru.spbau.mit.java.files.error.BlockNotPresent;
import ru.spbau.mit.java.files.error.FileNotExists;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;


/**
 * Storage, which can be queried for file parts.
 *
 * Every file goes along with it's identifier.
 */
public interface FileBlocksStorage {
    /**
     * Returns block size in bytes relevant for current blocks storage
     * This block size is mandatory for all file blocks except the last one...
     */
    default int getBlockSize() {
        return 10000;
    }

    /**
     * Assumes, that file is fully available on clients disk.
     * Adds data about file blocks marking all blocks as available (or present)
     * @param fileId file identifier
     * @param localFilePath path to local file assoc. with file info
     *
     * @throws ru.spbau.mit.java.files.error.FileIdClashError in case given file
     *         has the same if as any file already stored in storage
     */
    void addLocalFile(int fileId, String localFilePath) throws IOException;

    /**
     * Creates file filled with zeros (possibly) with size specified by {@code fileInfo}
     * Marks all the blocks of the file as unavailable (not downloaded yet)
     * @param fileId file identifier
     * @param localFilePath path to local file assoc. with file info
     * @param size file size in bytes
     */
    void createEmptyFile(int fileId, String localFilePath, long size) throws IOException;

    /**
     * Reads one block from specified file
     *
     * This method must always return array of size equal to {@code getBlockSize()}
     * return. In case this block is the last file block, not needed bytes must be
     * filled with zeros.
     *
     * @param fileId id of the file
     * @param blockId block index
     * @return bytes of the block
     *
     * @throws FileNotExists in case there is no file with id specified in the storage
     * @throws BlockNotPresent if queried block is not downloaded yet
     */
    byte[] readFileBlock(int fileId, int blockId) throws FileNotFoundException, IOException;

    /**
     * Writes one block to file, specified by fileId.
     *
     * @param fileId id of the file
     * @param blockId block index, where user wants to write data
     * @param block byte block data
     *
     * @throws FileNotExists in case file specified by {@code fileId} is not
     *         present in the storage
     * @throws BadBlockSize in case given block size is not equal to {@code getBlockSize()}
     *         return. The block must always be that size, even if this block is the last
     *         block of the file, but in this case Storage must write only
     *         prefix of this block to real file.
     */
    void writeFileBlock(int fileId, int blockId, byte[] block) throws IOException;

    /**
     * Returns list of file blocks available to start for specified file
     *
     * @param fileId id of the file to get blocks of
     * @return collection of block numbers
     *
     * @throws FileNotExists in case specified file is not exist
     */
    Collection<Integer> getAvailableFileBlocks(int fileId);

    /**
     * Returns collection of file ids, which are avaiable (at least one
     * block of the file) in the storage
     *
     * @return collection of file ids
     */
    List<Integer> getAvailableFileIds();

    /**
     * Checks if file with given id is already in sotrage (it may be
     * both fully downloaded or not)
     * @param fileId id of the file
     */
    boolean isFileInStorage(int fileId);

    /**
     *
     * @param fileId id of the file
     * @return number of file blocks available in storage for given file
     */
    int getAvailableFileBlocksNumber(int fileId);

    /**
     * @param fileId id of the file
     * @return total block number in given file
     */
    int getTotalBlockNumber(int fileId);

    /**
     *
     * @param fileId
     * @return local file path of the file
     */
    String getLocalFilePath(int fileId);

    /**
     *
     * @param fileId
     * @return true if all file blocks are downloaded
     */
    boolean isFileFullyAvailable(int fileId);
}
