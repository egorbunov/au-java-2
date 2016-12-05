package ru.spbau.mit.java.leech;


import java.io.IOException;
import java.util.Collection;

public interface SeederConnection {
    Collection<Integer> stat(int fileId) throws IOException;
    byte[] downloadFileBlock(int fileId, int blockId) throws IOException;
    void disconnect() throws IOException;
}
