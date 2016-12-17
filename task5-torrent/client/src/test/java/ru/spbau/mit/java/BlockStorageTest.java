package ru.spbau.mit.java;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.files.error.BadBlockSize;
import ru.spbau.mit.java.files.error.FileNotExistsInStorage;

import java.io.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockStorageTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void simpleBlockStorageAddLocalTest() throws IOException {
        FileBlocksStorage s = new SimpleBlockStorage(42);

        Random r = new Random();
        File f = tmp.newFile("file.txt");
        int fileId = 1;
        byte[] content = new byte[s.getBlockSize() * 3];
        r.nextBytes(content);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        out.write(content);
        out.close();

        s.addLocalFile(fileId, f.getPath());

        Collection<Integer> fileBlocks = s.getAvailableFileBlocks(fileId);
        Integer[] actual = fileBlocks.stream().sorted().collect(Collectors.toList()).toArray(new Integer[]{0});
        Assert.assertArrayEquals(actual, new Integer[] {0, 1, 2});

        Assert.assertEquals(1, s.getAvailableFileIds().size());
        Assert.assertEquals(1, (int) s.getAvailableFileIds().iterator().next());
    }

    @Test
    public void simpleBlockStorageAddNewTest() throws IOException, BadBlockSize {
        FileBlocksStorage s = new SimpleBlockStorage(42);
        Path fp = tmp.getRoot().toPath().resolve("file.txt");
        s.createEmptyFile(1, fp.toString(), s.getBlockSize() * 3 + 42);
        Collection<Integer> availableBlocks = s.getAvailableFileBlocks(1);
        Assert.assertTrue(availableBlocks.isEmpty());

        byte[] content = new byte[42];
        Random r = new Random();
        r.nextBytes(content);

        int blockId = 3;
        s.writeFileBlock(1, blockId, content);
        byte[] actual = s.readFileBlock(1, blockId);

        Assert.assertArrayEquals(content, actual);
        Assert.assertEquals(1, s.getAvailableFileIds().size());
        Assert.assertEquals(1, (int) s.getAvailableFileIds().iterator().next());

        Assert.assertEquals(1, s.getAvailableFileIds().size());
        Assert.assertEquals(1, (int) s.getAvailableFileIds().iterator().next());
    }
}
