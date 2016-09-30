package ru.spbau.mit.java.wit.test;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitAdd;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class AddTest {
    @Rule
    public TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private WitAdd addCmd;
    private Path baseDir;


    @Before
    public void setup() {
        WitInit init = new WitInit();
        baseDir = baseFolder.getRoot().toPath();
        init.execute(baseDir, null);
        Path witRoot = WitInit.findRepositoryRoot(baseDir);
        storage = new WitStorage(witRoot);
        addCmd = new WitAdd();
        addCmd.fileNames = new ArrayList<>();
    }

    private void checkStaged(List<File> files) {
        Set<String> stagedFiles = new HashSet<>(
                WitUtils.getStagedEntries(storage.readIndex())
                        .map(e -> e.fileName)
                        .collect(Collectors.toList())
        );
        Set<String> actual = new HashSet<>(files.stream()
                .map(it -> baseDir.relativize(it.toPath()))
                .map(Path::toString)
                .collect(Collectors.toList()));
        Assert.assertEquals(actual.size(), stagedFiles.size());
        for (String s : stagedFiles) {
            Assert.assertTrue(actual.contains(s));
        }
    }

    @Test
    public void testAddNothing() {
        addCmd.execute(baseDir, storage);
        checkStaged(Collections.emptyList());
    }

    @Test
    public void testAddOneFile() throws IOException {
        File f = baseFolder.newFile();
        addCmd.fileNames.add(f.toString());
        addCmd.execute(baseDir, storage);

        checkStaged(Collections.singletonList(f));
    }

    @Test
    public void testAddNTimes() throws IOException {
        File f = baseFolder.newFile();
        addCmd.fileNames.add(f.toString());
        addCmd.execute(baseDir, storage);
        addCmd.execute(baseDir, storage);
        addCmd.execute(baseDir, storage);

        checkStaged(Collections.singletonList(f));
    }

    @Test
    public void testAddNotInRepo() throws IOException {
        Path f = Files.createTempFile(null, null);
        addCmd.fileNames.add(f.toString());
        addCmd.execute(baseDir, storage);

        checkStaged(Collections.emptyList());
    }

    /**
     * FIXME: very strange behavior, because last modification time
     *        does not changes in spite of Thread.sleep call
     */
    @Test
    public void testAddUpdate() throws IOException, InterruptedException {
        File f = baseFolder.newFile();
        addCmd.fileNames.add(f.toString());
        addCmd.execute(baseDir, storage);
        FileUtils.writeLines(f, Collections.singletonList("int main() { return 0; }"));
        Thread.sleep(500);
        addCmd.execute(baseDir, storage);


        checkStaged(Collections.singletonList(f));
    }

    @Test
    public void testAddDirectory() throws IOException {
        Path subFolder = Files.createDirectories(baseDir.resolve("sub_folder"));
        List<Path> files = Arrays.asList(
                Files.createFile(subFolder.resolve("file.txt")),
                Files.createFile(subFolder.resolve("txt.file")));

        addCmd.fileNames.add(baseDir.toString());
        addCmd.execute(baseDir, storage);

        checkStaged(files.stream().map(Path::toFile).collect(Collectors.toList()));
    }
}
