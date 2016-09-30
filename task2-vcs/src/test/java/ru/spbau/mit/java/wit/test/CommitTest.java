package ru.spbau.mit.java.wit.test;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.AddCmd;
import ru.spbau.mit.java.wit.command.CommitCmd;
import ru.spbau.mit.java.wit.command.LogCmd;
import ru.spbau.mit.java.wit.storage.WitInit;
import ru.spbau.mit.java.wit.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class CommitTest {
    @Rule
    public TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private AddCmd addCmd;
    private CommitCmd commitCmd;
    private Path baseDir;


    @Before
    public void setup() {
        baseDir = baseFolder.getRoot().toPath();
        Path witRoot = WitInit.init(baseDir);
        storage = new WitStorage(witRoot);
        addCmd = new AddCmd();
        addCmd.fileNames = new ArrayList<>();
        commitCmd = new CommitCmd();
        commitCmd.msg = "HELLO";
    }

    @Test
    public void testCommitNothing() {
        commitCmd.run(baseDir, storage);
    }

    @Test
    public void testCommitOneFile() throws IOException {
        File f = baseFolder.newFile();
        addCmd.fileNames.add(f.toString());
        addCmd.run(baseDir, storage);
        commitCmd.run(baseDir, storage);
    }

    @Test
    public void testCommitMany() throws IOException {
        Path subFolder = Files.createDirectories(baseDir.resolve("sub_folder"));
        List<Path> files = Arrays.asList(
                Files.createFile(subFolder.resolve("file.txt")),
                Files.createFile(subFolder.resolve("txt.file")));
        addCmd.fileNames.add(baseDir.toString());
        addCmd.run(baseDir, storage);
        commitCmd.run(baseDir, storage);
    }
}
