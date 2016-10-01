package ru.spbau.mit.java.wit.test;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitCheckout;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.command.WitMerge;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Created by: Egor Gorbunov
 * Date: 10/1/16
 * Email: egor-mailbox@ya.com
 */
public class MergeTest {
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
    public void testMerge() throws IOException {
        String master = storage.readCurBranchName();
        witUtil.commitFile("x.txt", Arrays.asList(
                "int main()",
                "{",
                "    return 0;",
                "return 0;}",
                "asdasda",
                "oh my god",
                ";}"));

        String branch = witUtil.createBranch();
        checkoutRef(branch);
        witUtil.commitFile("x.txt", Arrays.asList(
                "int main()",
                "",
                "{",
                "    return 1;",
                "asdasda", ";}"));
        checkoutRef(master);
        merge(branch);
    }

    private void checkoutRef(String br) throws IOException {
        WitCheckout checkoutNewBranch = new WitCheckout();
        checkoutNewBranch.setRef(br);
        checkoutNewBranch.execute(userRepoDir, storage);
    }

    private void merge(String br) throws IOException {
        WitMerge merge = new WitMerge();
        merge.setBranch(br);
        merge.execute(userRepoDir, storage);
    }
}
