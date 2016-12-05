package ru.spbau.mit.java.files.error;

public class BlockNotPresent extends RuntimeException {
    private final int fileId;
    private final int blockId;

    public BlockNotPresent(int fileId, int blockId) {
        this.fileId = fileId;
        this.blockId = blockId;
    }

    public int getFileId() {
        return fileId;
    }

    public int getBlockId() {
        return blockId;
    }
}
