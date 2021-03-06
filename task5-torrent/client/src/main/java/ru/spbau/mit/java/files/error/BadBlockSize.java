package ru.spbau.mit.java.files.error;


public class BadBlockSize extends Exception {
    private final int fileId;
    private final int blockId;
    private final int actualBlockSize;
    private final int expectedBlockSize;

    public BadBlockSize(int fileId, int blockId, int actualBlockSize, int expectedBlockSize) {

        this.fileId = fileId;
        this.blockId = blockId;
        this.actualBlockSize = actualBlockSize;
        this.expectedBlockSize = expectedBlockSize;
    }

    public int getExpectedBlockSize() {
        return expectedBlockSize;
    }

    public int getActualBlockSize() {
        return actualBlockSize;
    }

    public int getBlockId() {
        return blockId;
    }

    public int getFileId() {
        return fileId;
    }
}
