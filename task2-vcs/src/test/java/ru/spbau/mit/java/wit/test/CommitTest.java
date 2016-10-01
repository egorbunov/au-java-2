package ru.spbau.mit.java.wit.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitAdd;
import ru.spbau.mit.java.wit.command.WitCommit;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class CommitTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private WitAdd addCmd;
    private WitCommit commitCmd;
    private Path baseDir;


    @Before
    public void setup() throws IOException {
        WitInit init = new WitInit();
        baseDir = baseFolder.getRoot().toPath();
        init.execute(baseDir, null);
        Path witRoot = WitInit.findRepositoryRoot(baseDir);
        storage = new WitStorage(witRoot);
        addCmd = new WitAdd();
        commitCmd = new WitCommit();
        commitCmd.setMsg("HELLO");
    }

    private void checkNothingStagedForCommit() throws IOException {
        Assert.assertEquals(0, WitUtils.getStagedEntries(storage.readIndex())
                .map(e -> e.fileName).count());
    }

    @Test
    public void testCommitNothing() throws IOException {
        commitCmd.execute(baseDir, storage);
        checkNothingStagedForCommit();
    }

    @Test
    public void testCommitOneFile() throws IOException {
        File f = baseFolder.newFile();
        addCmd.setFileNames(Collections.singletonList(f.toString()));
        addCmd.execute(baseDir, storage);
        commitCmd.execute(baseDir, storage);

        checkNothingStagedForCommit();
    }

    @Test
    public void testCommitDir() throws IOException {
        Path subFolder = Files.createDirectories(baseDir.resolve("sub_folder"));
        Arrays.asList(
                Files.createFile(baseDir.resolve("1.txt")),
                Files.createFile(subFolder.resolve("file.txt")),
                Files.createFile(subFolder.resolve("txt.file"))
        );
        addCmd.setFileNames(Collections.singletonList(baseDir.toString()));
        addCmd.execute(baseDir, storage);
        commitCmd.execute(subFolder, storage);

        checkNothingStagedForCommit();
    }
}
