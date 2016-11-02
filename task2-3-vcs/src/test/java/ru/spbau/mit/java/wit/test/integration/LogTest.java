package ru.spbau.mit.java.wit.test.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class LogTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private WitTestUtil witUtil;

    @Before
    public void setup() throws IOException {
        WitInit init = new WitInit();
        Path userRepoDir = baseFolder.getRoot().toPath();
        init.execute(userRepoDir, null);
        Path witRoot = WitInit.findRepositoryRoot(userRepoDir);
        storage = new WitStorage(witRoot);
        witUtil = new WitTestUtil(userRepoDir, storage);
    }


    @Test
    public void testSimpleHistory() throws IOException {
        List<ShaId> expectedLog = new ArrayList<>();

        expectedLog.add(witUtil.commitFile("1.txt"));
        expectedLog.add(witUtil.commitFile("2.txt"));
        expectedLog.add(witUtil.commitFile("3.txt"));

        List<ShaId> actualLog = storage.readCommitLog(storage.readCurBranchName());

        Assert.assertArrayEquals(
                expectedLog.toArray(),
                actualLog.toArray()
        );
    }

    @Test
    public void testLogAfterBranch() throws IOException {
        List<ShaId> expectedLog = new ArrayList<>();

        expectedLog.add(witUtil.commitFile("1.txt"));
        expectedLog.add(witUtil.commitFile("2.txt"));

        String brName = witUtil.createBranch();

        witUtil.commitFile("3.txt");
        witUtil.commitFile("4.txt");

        List<ShaId> actualLog = storage.readCommitLog(brName);

        Assert.assertArrayEquals(
                expectedLog.toArray(),
                actualLog.toArray()
        );
    }
}
