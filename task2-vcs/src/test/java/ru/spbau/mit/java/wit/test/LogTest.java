package ru.spbau.mit.java.wit.test;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitAdd;
import ru.spbau.mit.java.wit.command.WitBranch;
import ru.spbau.mit.java.wit.command.WitCommit;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class LogTest {
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
    }


    @Test
    public void testSimpleHistory() throws IOException {
        List<ShaId> expectedLog = new ArrayList<>();

        expectedLog.add(commitFile("1.txt", ""));
        expectedLog.add(commitFile("2.txt", ""));
        expectedLog.add(commitFile("3.txt", ""));

        List<ShaId> actualLog = storage.readCommitLog(storage.readCurBranchName());

        Assert.assertArrayEquals(
                expectedLog.toArray(),
                actualLog.toArray()
        );
    }

    @Test
    public void testLogAfterBranch() throws IOException {
        List<ShaId> expectedLog = new ArrayList<>();

        expectedLog.add(commitFile("1.txt", ""));
        expectedLog.add(commitFile("2.txt", ""));

        String brName = createBranch();

        commitFile("3.txt", "");
        commitFile("4.txt", "");

        List<ShaId> actualLog = storage.readCommitLog(brName);

        Assert.assertArrayEquals(
                expectedLog.toArray(),
                actualLog.toArray()
        );
    }


    private Path writeFile(String file, String content) throws IOException {
        Path p = userRepoDir.resolve(file);
        Files.createDirectories(p.getParent());
        FileUtils.writeLines(p.toFile(), Collections.singletonList(content));
        return p;
    }

    private ShaId commitFile(String file, String content) throws IOException {
        Path p = writeFile(file, content);
        WitAdd addCmd = new WitAdd();
        addCmd.setFileNames(Collections.singletonList(p.toString()));
        addCmd.execute(userRepoDir, storage);
        WitCommit commitCmd = new WitCommit();
        commitCmd.setMsg(content + "_" + UUID.randomUUID().toString());
        commitCmd.execute(userRepoDir, storage);
        return storage.readBranch(storage.readCurBranchName()).getHeadCommitId();
    }

    private String createBranch() throws IOException {
        String name = UUID.randomUUID().toString();
        WitBranch bCmd = new WitBranch();
        bCmd.setBranchName(name);
        bCmd.execute(userRepoDir, storage);
        return name;
    }



}
