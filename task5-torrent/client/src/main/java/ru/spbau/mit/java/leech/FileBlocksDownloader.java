package ru.spbau.mit.java.leech;


import java.io.IOException;

public interface FileBlocksDownloader {
    /**
     * Starts downloading
     */
    void start() throws IOException;

    /**
     * Terminates file downloading
     */
    void stop();

    /**
     * Resumes file downloading
     */
    void resume();

    /**
     * Blocks until downloading ended
     */
    void join() throws InterruptedException;

    /**
     * Total block number to start
     */
    int goalBlockNum();

    /**
     * already downloaded block num
     */
    int downloadedBlockNum();
}
