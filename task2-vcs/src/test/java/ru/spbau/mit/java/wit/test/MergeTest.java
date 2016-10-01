package ru.spbau.mit.java.wit.test;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @Before
    public void setup() throws IOException {
        WitInit init = new WitInit();
        userRepoDir = baseFolder.getRoot().toPath();
        init.execute(userRepoDir, null);
        Path witRoot = WitInit.findRepositoryRoot(userRepoDir);
        storage = new WitStorage(witRoot);
    }

    @Test
    public void testMerge() throws IOException {
        String master = storage.readCurBranchName();
        commitFile("x.txt", Arrays.asList(
                "int main()",
                "{",
                "    return 0;",
                "return 0;}",
                "asdasda",
                "oh my god",
                ";}"));

        String branch = createBranch();
        checkoutRef(branch);
        commitFile("x.txt", Arrays.asList(
                "int main()",
                "",
                "{",
                "    return 1;",
                "asdasda", ";}"));
        checkoutRef(master);
        merge(branch);
    }

    private Path writeFile(String file, List<String> content) throws IOException {
        Path p = userRepoDir.resolve(file);
        Files.createDirectories(p.getParent());
        FileUtils.writeLines(p.toFile(), content);
        return p;
    }

    private ShaId commitFile(String file, List<String> content) throws IOException {
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
