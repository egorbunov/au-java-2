package ru.spbau.mit.java.wit.test.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.repository.WitStatusUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
public class ResetTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    private WitStorage storage;
    private Path userRepoDir;
    private WitTestUtil witUtil;

    @Before
    public void setup() throws IOException {
        WitInit init = new WitInit();
        userRepoDir = baseFolder.getRoot().toPath();
        init.execute(userRepoDir, null);
        Path witRoot = WitInit.findRepositoryRoot(userRepoDir);
        storage = new WitStorage(witRoot);
        witUtil = new WitTestUtil(userRepoDir, storage);
    }

    @Test
    public void testReset() throws IOException {
        List<String> files = Arrays.asList("file.txt", "folder/file.txt", "xxx.cpp");
        for (String f : files) {
            witUtil.addFile(f);
        }
        for (String f : files) {
            witUtil.resetFile(f);
        }
        Assert.assertEquals(0,
                WitStatusUtils.getStagedEntries(storage.readIndex()).count());
    }
}
