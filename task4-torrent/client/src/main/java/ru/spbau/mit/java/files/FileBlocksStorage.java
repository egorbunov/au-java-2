package ru.spbau.mit.java.files;

import ru.spbau.mit.java.files.error.BadBlockSize;
import ru.spbau.mit.java.files.error.BlockNotPresent;
import ru.spbau.mit.java.files.error.FileNotExists;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

public abstract class FileBlocksStorage {
    /**
     * Returns block size in bytes relevant for current blocks storage
     * This block size is mandatory for all file blocks except the last one...
     */
    final int getBlockSizeInBytes() {
        return 100000;
    }

    /**
     * Assumes, that file is fully available on clients disk.
     * Adds data about file blocks marking all blocks as available (or present)
     * @param fileInfo associated with local file tracker file info
     * @param localFilePath path to local file assoc. with file info
     */
    abstract void addLocalFile(TrackerFile<Integer> fileInfo, String localFilePath);

    /**
     * Creates file filled with zeros with size specified by {@code fileInfo}
     * Marks all the blocks of the file as unavailable (not downloaded yet)
     * @param fileInfo associated with local file tracker file info
     * @param localFilePath path to local file assoc. with file info
     */
    abstract void createEmptyFile(TrackerFile<Integer> fileInfo, String localFilePath);

    /**
     * Reads one block from specified file
     *
     *
     * @param fileId id (as specified by {@see TrackerFile} of the file
     * @param blockId block index
     * @return bytes of the block
     *
     * @throws FileNotExists in case there is no file with id specified in the storage
     * @throws BlockNotPresent if queried block is not downloaded yet
     */
    abstract byte[] readFileBlock(int fileId, int blockId);

    /**
     * Writes one block to file, specified by fileId.
     *
     * @param fileId id (as specified by {@see TrackerFile} of the file
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
    abstract void writeFileBlock(int fileId, int blockId, byte[] block);
}
