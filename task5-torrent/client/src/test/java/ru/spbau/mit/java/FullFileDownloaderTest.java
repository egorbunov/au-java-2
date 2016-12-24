package ru.spbau.mit.java;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.files.error.BlockNotPresent;
import ru.spbau.mit.java.leech.FullFileDownloader;
import ru.spbau.mit.java.leech.SeederConnection;
import ru.spbau.mit.java.leech.SeederConnectionFactory;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FullFileDownloaderTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private final int fileId = 42;
    private FileBlocksStorage fileBlocksStorage = new SimpleBlockStorage(10000);
    private final int fileBlockNum = 5000;
    private final int fileSize = fileBlocksStorage.getBlockSize() * fileBlockNum;
    private final List<Integer> fileBlocks = IntStream.range(0, fileBlockNum).boxed().collect(Collectors.toList());
    private final byte blockByte = 42;

    private SeederConnectionFactory<Integer> seederConnectionFactory = clientId -> new SeederConnection() {
        @Override
        public Collection<Integer> stat(int fileId) throws IOException {
            return fileBlocks;
        }

        @Override
        public byte[] downloadFileBlock(int fileId, int blockId) throws IOException {
            byte[] block = new byte[fileBlocksStorage.getBlockSize()];
            for (int i = 0; i < block.length; ++i) {
                block[i] = blockByte;
            }
            return block;
        }

        @Override
        public void disconnect() throws IOException {}
    };

    private Tracker<Integer, Integer> tracker = new Tracker<Integer, Integer>() {
        @Override
        public List<TrackerFile<Integer>> list() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(Integer clientId, List<Integer> fileIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer upload(FileInfo fileInfo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Integer> source(Integer fileId) {
            return Arrays.asList(1, 2, 3, 4, 5);
        }

        @Override
        public void removeClient(Integer clientId) {
            throw new UnsupportedOperationException();
        }
    };

    @Test
    public void testStopResume() throws IOException, InterruptedException, BlockNotPresent {
        FullFileDownloader<Integer> fileDownloader = new FullFileDownloader<>(
                fileId,
                fileSize,
                tmp.getRoot().toPath().resolve("file.txt").toString(),
                fileBlocksStorage,
                tracker,
                seederConnectionFactory
        );

        fileDownloader.start();

        for (int i = 0; i < 10; ++i) {
            fileDownloader.stop();
            Thread.sleep(50);
            fileDownloader.resume();
        }

        fileDownloader.join();

        Assert.assertTrue(fileBlocksStorage.isFileInStorage(fileId));
        Collection<Integer> blocks = fileBlocksStorage.getAvailableFileBlocks(fileId);

        Assert.assertArrayEquals(
                fileBlocks.toArray(new Integer[]{}),
                blocks.stream().sorted().collect(Collectors.toList()).toArray(new Integer[] {}));

        for (Integer id : fileBlocks) {
            byte[] bytes = fileBlocksStorage.readFileBlock(fileId, id);
            Assert.assertEquals(fileBlocksStorage.getBlockSize(), bytes.length);
            for (byte x : bytes) {
                Assert.assertEquals(blockByte, x);
            }
        }
    }
}
