package ru.spbau.mit.java.files;


import ru.spbau.mit.java.files.error.BadBlockSize;
import ru.spbau.mit.java.files.error.BlockNotPresent;
import ru.spbau.mit.java.files.error.FileIdClashError;
import ru.spbau.mit.java.files.error.FileNotExists;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SimpleBlockStorage implements FileBlocksStorage {
    private class FileData {
        private final String localPath;
        private final int size;
        private final Set<Integer> availableBlockIds;

        private FileData(String localPath, long size, Set<Integer> availableBlockIds) {
            this.localPath = localPath;
            this.size = (int) size;
            this.availableBlockIds = availableBlockIds;
        }

        private int blockSize() {
            return SimpleBlockStorage.this.getBlockSizeInBytes();
        }

        private int getBlocksNum() {
            int blockCnt = size / blockSize();
            if (size % blockSize() != 0) {
                blockCnt += 1;
            }
            return blockCnt;
        }

        int getLastBlockSize() {
            int lastPartSize = size % blockSize();
            if (lastPartSize == 0) {
                lastPartSize = blockSize();
            }
            return lastPartSize;
        }

        int getBlockSize(int blockId) {
            checkBlockValid(blockId);
            if (blockId < getBlocksNum() - 1) {
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

    private Map<Integer, FileData> files = new HashMap<>();

    @Override
    public void addLocalFile(int fileId, String localFilePath) throws IOException {
        if (files.containsKey(fileId)) {
            throw new FileIdClashError(fileId);
        }

        long size = Files.size(Paths.get(localFilePath));
        int partCnt = (int) size / getBlockSizeInBytes();
        if (size % getBlockSizeInBytes() != 0) {
            partCnt += 1;
        }


        Set<Integer> partsList = IntStream.range(0, partCnt).boxed()
                .collect(Collectors.toSet());

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

        RandomAccessFile f = new RandomAccessFile(localFilePath, "w");
        f.setLength(size);
        f.close();

        files.put(fileId, new FileData(localFilePath, size, new HashSet<>()));
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
        f.seek(getBlockSizeInBytes() * blockId);
        byte[] part = new byte[fd.getBlockSize(blockId)];
        int bs = f.read(part);
        assert bs == part.length;
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
        if (block.length != fd.getBlockSize(blockId)) {
            throw new BadBlockSize(fileId, blockId, block.length, fd.getBlockSize(blockId));
        }
        RandomAccessFile f = new RandomAccessFile(fd.localPath, "w");
        f.write(block);
    }

    @Override
    public Collection<Integer> getAvailableFileBlocks(int fileId) {
        if (!files.containsKey(fileId)) {
            throw new FileNotExists(fileId);
        }
        return files.get(fileId).getAvailableBlockIds();
    }

    @Override
    public Collection<Integer> getAvailableFileIds() {
        return Collections.unmodifiableCollection(files.keySet());
    }
}
