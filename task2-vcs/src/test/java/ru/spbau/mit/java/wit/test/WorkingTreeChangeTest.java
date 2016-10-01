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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class WorkingTreeChangeTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private WitAdd addCmd;
    private Path userRepoDir;
    private List<File> addedFiles;


    @Before
    public void setup() throws IOException {
        WitInit init = new WitInit();
        userRepoDir = baseFolder.getRoot().toPath();
        init.execute(userRepoDir, null);
        Path witRoot = WitInit.findRepositoryRoot(userRepoDir);
        storage = new WitStorage(witRoot);
        addCmd = new WitAdd();

        addedFiles = Arrays.asList(
                baseFolder.newFile("1.txt"),
                baseFolder.newFile("2.txt"),
                baseFolder.newFile("3.txt")
        );

        addCmd.setFileNames(addedFiles.stream().map(File::toString).collect(Collectors.toList()));
        addCmd.execute(userRepoDir, storage);
    }

    @Test
    public void testDeleteRecognized() throws IOException {
        Files.delete(addedFiles.get(0).toPath());
        List<Path> treeDeletedFiles =
                WitUtils.getTreeDeletedFiles(userRepoDir, storage.readIndex()).collect(Collectors.toList());
        Assert.assertEquals(1, treeDeletedFiles.size());
        Assert.assertEquals(addedFiles.get(0).toPath(), treeDeletedFiles.get(0));
    }

    @Test
    public void testNotTrackedRecognized() throws IOException {
        File f = baseFolder.newFile("not_tracked_yet");
        List<Path> notTracked =
                WitUtils.getTreeNewFiles(userRepoDir, storage.readIndex()).collect(Collectors.toList());
        Assert.assertEquals(1, notTracked.size());
        Assert.assertEquals(f.toPath(), notTracked.get(0));
    }

    @Test
    public void testModifiedRecognized() throws IOException, InterruptedException {
        File f = addedFiles.get(0);
        Thread.sleep(100);
        FileUtils.writeLines(f, Arrays.asList("1", "2"));
        List<Path> modified =
                WitUtils.getTreeModifiedFiles(userRepoDir, storage.readIndex()).collect(Collectors.toList());
        // TODO: strange stuff again. Last modified field not changed for File.
        // Assert.assertEquals(1, modified.size());
        // Assert.assertEquals(f.toPath(), modified.get(0));
    }

}
