package ru.spbau.mit.java.wit.test;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.AddCmd;
import ru.spbau.mit.java.wit.storage.WitInit;
import ru.spbau.mit.java.wit.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class AddTest {
    @Rule
    public TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private AddCmd addCmd;
    private Path baseDir;


    @Before
    public void setup() {
        baseDir = baseFolder.getRoot().toPath();
        Path witRoot = WitInit.init(baseDir);
        storage = new WitStorage(witRoot);
        addCmd = new AddCmd();
        addCmd.fileNames = new ArrayList<>();
    }

    @Test
    public void testAddNothing() {
        addCmd.run(baseDir, storage);
    }

    @Test
    public void testAddOneFile() throws IOException {
        File f = baseFolder.newFile();
        addCmd.fileNames.add(f.toString());
        addCmd.run(baseDir, storage);
    }

    @Test
    public void testAddNTimes() throws IOException {
        File f = baseFolder.newFile();
        addCmd.fileNames.add(f.toString());
        addCmd.run(baseDir, storage);
        addCmd.run(baseDir, storage);
        addCmd.run(baseDir, storage);
    }

    @Test
    public void testAddNotInRepo() throws IOException {
        Path f = Files.createTempFile(null, null);
        addCmd.fileNames.add(f.toString());
        addCmd.run(baseDir, storage);
    }

    /**
     * very strange behavior, because last modification time
     * does not changes in spite of Thread.sleep call
     */
    @Test
    public void testAddUpdate() throws IOException, InterruptedException {
        File f = baseFolder.newFile();
        addCmd.fileNames.add(f.toString());
        addCmd.run(baseDir, storage);
        FileUtils.writeLines(f, Collections.singletonList("int main() { return 0; }"));
        Thread.sleep(500);
        addCmd.run(baseDir, storage);
    }

    @Test
    public void testAddDirectory() throws IOException {
        Path subFolder = Files.createDirectories(baseDir.resolve("sub_folder"));
        List<Path> files = Arrays.asList(
                Files.createFile(subFolder.resolve("file.txt")),
                Files.createFile(subFolder.resolve("txt.file")));
        addCmd.fileNames.add(baseDir.toString());
        addCmd.run(baseDir, storage);
    }
}
