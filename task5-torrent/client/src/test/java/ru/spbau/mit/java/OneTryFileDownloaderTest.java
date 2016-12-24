package ru.spbau.mit.java;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.leech.OneTryFileBlocksDownloader;
import ru.spbau.mit.java.leech.SeederConnection;
import ru.spbau.mit.java.leech.SeederConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class OneTryFileDownloaderTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private final int fileId = 42;
    private FileBlocksStorage fileBlocksStorage = new SimpleBlockStorage(1000);
    private final int fileSize = fileBlocksStorage.getBlockSize() * 10;
    private final List<Integer> fileBlocks = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    private SeederConnectionFactory<Integer> seederConnectionFactory = clientId -> new SeederConnection() {
        @Override
        public Collection<Integer> stat(int fileId) throws IOException {
            return fileBlocks;
        }

        @Override
        public byte[] downloadFileBlock(int fileId, int blockId) throws IOException {
            byte[] block = new byte[fileBlocksStorage.getBlockSize()];
            for (int i = 0; i < block.length; ++i) {
                block[i] = 42;
            }
            return block;
        }

        @Override
        public void disconnect() throws IOException {}
    };

    @Test
    public void test() throws IOException, InterruptedException {
        OneTryFileBlocksDownloader<Integer> fileDownloader = new OneTryFileBlocksDownloader<>(
                fileId,
                fileSize,
                tmp.getRoot().toPath().resolve("file.txt").toString(),
                fileBlocksStorage,
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
                seederConnectionFactory
        );

        fileDownloader.start();
        fileDownloader.join();

        Assert.assertTrue(fileBlocksStorage.isFileInStorage(fileId));
        Collection<Integer> blocks = fileBlocksStorage.getAvailableFileBlocks(fileId);

        Assert.assertArrayEquals(
                fileBlocks.toArray(new Integer[]{}),
                blocks.stream().sorted().collect(Collectors.toList()).toArray(new Integer[] {}));
    }
}
