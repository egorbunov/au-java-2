package ru.spbau.mit.java.files;


import ru.spbau.mit.java.files.error.BadBlockSize;
import ru.spbau.mit.java.files.error.BlockNotPresent;
import ru.spbau.mit.java.files.error.FileIdClashError;
import ru.spbau.mit.java.files.error.FileNotExists;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * That is very simple block storage, which is implemented using
 * {@see RandomAccessFile} class.
 *
 * Some structures used in this implementations are concurrent due to
 * multithreaded nature of it's possible usage
 *
 * If I've got everything right we do not need to protect writeBlock or readBlock
 * methods from race conditions because every call works with it's own particular
 * file / part of the file.
 */
public class SimpleBlockStorage implements FileBlocksStorage, Serializable {
    private int blockSize;
    private Map<Integer, FileData> files = new ConcurrentHashMap<>();

    /**
     *
     * @param blockSize size of one block in bytes, this block is minimal readable/writable
     *                  entity
     */
    public SimpleBlockStorage(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public int getBlockSize() {
        return blockSize;
    }

    @Override
    public void addLocalFile(int fileId, String localFilePath) throws IOException {
        if (files.containsKey(fileId)) {
            throw new FileIdClashError(fileId);
        }

        long size = Files.size(Paths.get(localFilePath));
        int partCnt = (int) size / getBlockSize();
        if (size % getBlockSize() != 0) {
            partCnt += 1;
        }


        Set<Integer> partsList = new ConcurrentSkipListSet<>(IntStream.range(0, partCnt).boxed()
                .collect(Collectors.toSet()));

        files.put(fileId, new FileData(localFilePath, size, partsList));
    }

    @Override
    public void createEmptyFile(int fileId, String localFilePath, long size) throws IOException {
        if (files.containsKey(fileId)) {
            throw new FileIdClashError(fileId);
        }
        if (size < 0) {
            throw new IllegalArgumentException("Negative file size");
        }

        RandomAccessFile f = new RandomAccessFile(localFilePath, "rw");
        f.setLength(size);
        f.close();

        // here we use concurrent set of available bocks, because block write may happen
        // in multiple threads
        files.put(fileId, new FileData(localFilePath, size, new ConcurrentSkipListSet<>()));
    }

    @Override
    public byte[] readFileBlock(int fileId, int blockId) throws IOException {
        if (!files.containsKey(fileId)) {
            throw new FileNotExists(fileId);
        }
        FileData fd = files.get(fileId);
        if (!fd.isBlockAvailable(blockId)) {
            throw new BlockNotPresent(fileId, blockId);
        }
        RandomAccessFile f = new RandomAccessFile(fd.localPath, "r");
        f.seek(getBlockSize() * blockId);
        byte[] part = new byte[getBlockSize()];
        Arrays.fill(part, (byte) 0);
        int bs = f.read(part, 0, fd.getBlockSize(blockId));
        f.close();
        assert bs == fd.getBlockSize(blockId);
        return part;
    }

    @Override
    public void writeFileBlock(int fileId, int blockId, byte[] block) throws IOException {
        if (!files.containsKey(fileId)) {
            throw new FileNotExists(fileId);
        }
        FileData fd = files.get(fileId);
        if (fd.isBlockAvailable(blockId)) {
            throw new IllegalArgumentException("Block is already written");
        }
        if (block.length != getBlockSize()) {
            throw new BadBlockSize(fileId, blockId, block.length, getBlockSize());
        }
        RandomAccessFile f = new RandomAccessFile(fd.localPath, "rw");
        f.seek(getBlockSize() * blockId);
        f.write(block, 0, fd.getBlockSize(blockId));
        f.close();

        // make block available
        fd.addAvailableBlock(blockId);
    }

    @Override
    public Collection<Integer> getAvailableFileBlocks(int fileId) {
        if (!files.containsKey(fileId)) {
            throw new FileNotExists(fileId);
        }
        return files.get(fileId).getAvailableBlockIds();
    }

    @Override
    public List<Integer> getAvailableFileIds() {
        return files.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public boolean isFileInStorage(int fileId) {
        return files.containsKey(fileId);
    }

    @Override
    public int getAvailableFileBlocksNumber(int fileId) {
        if (!files.containsKey(fileId)) {
            throw new FileNotExists(fileId);
        }
        return files.get(fileId).getAvailableBlockIds().size();
    }

    @Override
    public int getTotalBlockNumber(int fileId) {
        if (!files.containsKey(fileId)) {
            throw new FileNotExists(fileId);
        }
        return files.get(fileId).getBlocksNum();
    }

    @Override
    public String getLocalFilePath(int fileId) {
        if (!files.containsKey(fileId)) {
            throw new FileNotExists(fileId);
        }
        return files.get(fileId).localPath;
    }

    @Override
    public boolean isFileFullyAvailable(int fileId) {
        return files.containsKey(fileId) && getAvailableFileBlocksNumber(fileId) == getTotalBlockNumber(fileId);
    }


    /**
     * Info about files stored in storage
     */
    private class FileData implements Serializable {
        private final String localPath;
        private final int size;
        private final Set<Integer> availableBlockIds;

        private FileData(String localPath, long size, Set<Integer> availableBlockIds) {
            this.localPath = localPath;
            this.size = (int) size;
            this.availableBlockIds = availableBlockIds;
        }

        private int blockSize() {
            return SimpleBlockStorage.this.getBlockSize();
        }

        private int getBlocksNum() {
            int blockCnt = size / blockSize();
            if (size % blockSize() != 0) {
                blockCnt += 1;
            }
            return blockCnt;
        }

        /**
         * size of last block is not equal to other blocks sizes
         */
        int getLastBlockSize() {
            int lastPartSize = size % blockSize();
            if (lastPartSize == 0) {
                lastPartSize = blockSize();
            }
            return lastPartSize;
        }

        /**
         * returns id of the last block
         */
        int getLastBlockId() {
            return getBlocksNum() - 1;
        }

        int getBlockSize(int blockId) {
            checkBlockValid(blockId);
            if (blockId < getLastBlockId()) {
                return blockSize();
            } else {
                return getLastBlockSize();
            }
        }

        private void checkBlockValid(int blockId) {
            if (blockId < 0 || blockId >= getBlocksNum()) {
                throw new IllegalArgumentException("Not valid block");
            }
        }

        public Set<Integer> getAvailableBlockIds() {
            return availableBlockIds;
        }

        boolean isBlockAvailable(int blockId) {
            checkBlockValid(blockId);
            return availableBlockIds.contains(blockId);
        }

        public void addAvailableBlock(int blockId) {
            if (availableBlockIds.contains(blockId)) {
                throw new IllegalArgumentException("Block already added");
            }
            checkBlockValid(blockId);
            availableBlockIds.add(blockId);
         }
    }

}
