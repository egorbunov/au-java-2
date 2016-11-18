package ru.spbau.mit.java.seed;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.util.Collection;

/**
 * Simple seeding request executor using {@link FileBlocksStorage}
 */
public class SeedingRequestExecutorImpl implements SeedingRequestExecutor {
    private FileBlocksStorage fileBlocksStorage;

    public SeedingRequestExecutorImpl(FileBlocksStorage fileBlocksStorage) {

        this.fileBlocksStorage = fileBlocksStorage;
    }

    @Override
    public StatResponse executeStat(StatRequest request) {
        Collection<Integer> blockIds = fileBlocksStorage.getAvailableFileBlocks(request.getFileId());
        return new StatResponse(blockIds);
    }

    @Override
    public GetPartResponse executeGetPart(GetPartRequest request) {
        byte[] block = fileBlocksStorage.readFileBlock(request.getFileId(), request.getPartId());
        return new GetPartResponse(block);
    }
}
