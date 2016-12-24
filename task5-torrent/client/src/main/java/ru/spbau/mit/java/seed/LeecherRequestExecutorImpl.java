package ru.spbau.mit.java.seed;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.error.BlockNotPresent;
import ru.spbau.mit.java.protocol.request.GetPartRequest;
import ru.spbau.mit.java.protocol.request.StatRequest;
import ru.spbau.mit.java.protocol.response.GetPartResponse;
import ru.spbau.mit.java.protocol.response.StatResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Simple seeding request executor using {@link FileBlocksStorage}
 */
public class LeecherRequestExecutorImpl implements LeecherRequestExecutor {
    private FileBlocksStorage fileBlocksStorage;
    Logger logger = Logger.getLogger(this.getClass().getName());

    public LeecherRequestExecutorImpl(FileBlocksStorage fileBlocksStorage) {
        this.fileBlocksStorage = fileBlocksStorage;
    }

    @Override
    public StatResponse executeStat(StatRequest request) {
        Collection<Integer> blockIds = fileBlocksStorage.getAvailableFileBlocks(request.getFileId());
        return new StatResponse(blockIds);
    }

    @Override
    public GetPartResponse executeGetPart(GetPartRequest request) throws IOException {
        byte[] block = new byte[0];
        try {
            block = fileBlocksStorage.readFileBlock(request.getFileId(), request.getPartId());
        } catch (BlockNotPresent blockNotPresent) {
            logger.severe("No such block in block storage error.");
            block = null;
        }
        return new GetPartResponse(block);
    }
}
