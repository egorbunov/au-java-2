package ru.spbau.mit.java.wit.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitAdd;
import ru.spbau.mit.java.wit.command.WitBranch;
import ru.spbau.mit.java.wit.command.WitCommit;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class BranchTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private Path userRepoDir;


    @Before
    public void setup() throws IOException {
        WitInit init = new WitInit();
        userRepoDir = baseFolder.getRoot().toPath();
        init.execute(userRepoDir, null);
        Path witRoot = WitInit.findRepositoryRoot(userRepoDir);
        storage = new WitStorage(witRoot);
        List<File> addedFiles = Arrays.asList(
                baseFolder.newFile("1.txt"),
                baseFolder.newFile("2.txt"),
                baseFolder.newFile("3.txt")
        );

        WitAdd addCmd = new WitAdd();
        addCmd.setFileNames(addedFiles.stream().map(File::toString).collect(Collectors.toList()));
        addCmd.execute(userRepoDir, storage);
    }

    @Test
    public void testBranchNoCommits() throws IOException {
        String branchName = "NEW_BRANCH";
        WitBranch branchCmd = new WitBranch();
        branchCmd.setNewBranchNames(Collections.singletonList(branchName));
        branchCmd.execute(userRepoDir, storage);
        Assert.assertNull(storage.readBranch(branchName));
    }

    @Test
    public void testBranchSimple() throws IOException {
        WitCommit commitCmd = new WitCommit();
        commitCmd.setMsg("MESSAGE");
        commitCmd.execute(userRepoDir, storage);

        String branchName = "NEW_BRANCH";
        WitBranch branchCmd = new WitBranch();
        branchCmd.setNewBranchNames(Collections.singletonList(branchName));
        branchCmd.execute(userRepoDir, storage);

        Branch master = storage.readBranch(storage.readCurBranchName());
        Branch newBranch = storage.readBranch(branchName);

        Assert.assertEquals(master.getHeadCommitId(), newBranch.getHeadCommitId());
    }
}
