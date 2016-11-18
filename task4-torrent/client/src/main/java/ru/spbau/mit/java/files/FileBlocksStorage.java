package ru.spbau.mit.java.files;

import ru.spbau.mit.java.files.error.BadBlockSize;
import ru.spbau.mit.java.files.error.BlockNotPresent;
import ru.spbau.mit.java.files.error.FileNotExists;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;


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
    default int getBlockSizeInBytes() {
        return 100000;
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
     * @throws BadBlockSize in case given block size is bad, that may happen when
     *         block is not last for the file (determined by blockId) and it's
     *         size not equal to {@code getBlockSizeInBytes()} return value;
     *         Or it may happen if the block is last and it's size is not equal
     *         to pre-calculated last block size of the file
     */
    void writeFileBlock(int fileId, int blockId, byte[] block) throws IOException;

    /**
     * Returns list of file blocks available to download for specified file
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
    Collection<Integer> getAvailableFileIds();
}
