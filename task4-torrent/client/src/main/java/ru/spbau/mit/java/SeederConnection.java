package ru.spbau.mit.java;


import java.util.Collection;

public interface SeederConnection {
    Collection<Integer> stat(int fileId);
    byte[] downloadFileBlock(int fileId, int blockId);
}

