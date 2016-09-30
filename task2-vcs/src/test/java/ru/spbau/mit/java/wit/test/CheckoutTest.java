package ru.spbau.mit.java.wit.test;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.*;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

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
public class CheckoutTest {
    @Rule
    public TemporaryFolder baseFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder actualFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder expectedFolder = new TemporaryFolder();


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

    private Path writeFile(String file, String content) throws IOException {
        Path p = userRepoDir.resolve(file);
        Files.createDirectories(p.getParent());
        FileUtils.writeLines(p.toFile(), Collections.singletonList(content));
        return p;
    }

    private void commitFile(String file, String content) throws IOException {
        Path p = writeFile(file, content);
        WitAdd addCmd = new WitAdd();
        addCmd.setFileNames(Collections.singletonList(p.toString()));
        addCmd.execute(userRepoDir, storage);
        WitCommit commitCmd = new WitCommit();
        commitCmd.setMsg(content + "_" + UUID.randomUUID().toString());
        commitCmd.execute(userRepoDir, storage);
    }

    private void createBranch(String name) throws IOException {
        WitBranch bCmd = new WitBranch();
        bCmd.setBranchName(name);
        bCmd.execute(userRepoDir, storage);
    }

    private void copyRepoToFolder(Path p) throws IOException {
        FileUtils.copyDirectory(baseFolder.getRoot().toPath().toFile(), p.toFile());
        FileUtils.deleteDirectory(WitUtils.resolveStoragePath(p).toFile());
    }

    private void checkFoldersEqual() throws IOException {
        Path actual = actualFolder.getRoot().toPath();
        Path expected = expectedFolder.getRoot().toPath();

        List<Path> exp =
                Files.walk(expected)
                        .filter(p -> !p.equals(expected))
                        .map(p -> p.subpath(expected.getNameCount(), p.getNameCount()))
                        .sorted(Comparator.comparing(Path::toString))
                        .collect(Collectors.toList());
        List<Path> act =
                Files.walk(actual)
                        .filter(p -> !p.equals(actual))
                        .map(p -> p.subpath(actual.getNameCount(), p.getNameCount()))
                        .sorted(Comparator.comparing(Path::toString))
                        .collect(Collectors.toList());

        Assert.assertEquals(exp.size(), act.size());
        for (int i = 0; i < exp.size(); ++i) {
            Path e = exp.get(i);
            Path a = act.get(i);
            Assert.assertEquals(e, a);

            if (Files.isRegularFile(expected.resolve(a)) ||
                    Files.isRegularFile(actual.resolve(e))) {
                Path ae = expected.resolve(e);
                Path aa = actual.resolve(a);
                Assert.assertTrue(Files.isRegularFile(ae));
                Assert.assertTrue(Files.isRegularFile(aa));

                FileUtils.contentEquals(ae.toFile(), aa.toFile());
            }
        }
    }

    private void checkoutRef(String br) throws IOException {
        WitCheckout checkoutNewBranch = new WitCheckout();
        checkoutNewBranch.setRef(br);
        checkoutNewBranch.execute(userRepoDir, storage);
    }


    @Test
    public void testCheckoutBranchSimple() throws IOException {
        String branchName = "new_branch";
        commitFile("1.txt", "hello");
        commitFile("fold/2.txt", "xxx");
        createBranch(branchName);

        copyRepoToFolder(expectedFolder.getRoot().toPath());
        String initBranch = storage.readCurBranchName();

        checkoutRef(branchName);
        commitFile("2.txt", "hi");

        checkoutRef(initBranch);

        copyRepoToFolder(actualFolder.getRoot().toPath());

        checkFoldersEqual();
    }

    @Test
    public void testCheckoutBackAfterCommits() throws IOException {
        String masterBranch = storage.readCurBranchName();
        String branchName = "new";
        commitFile("1.txt", "HELLO");
        createBranch(branchName);

        checkoutRef(branchName);
        copyRepoToFolder(expectedFolder.getRoot().toPath());
        checkoutRef(masterBranch);

        commitFile("2.txt", "");
        commitFile("fold/3.txt", "stuff");
        commitFile("xxx/xxx/xxx.avi", "blocked_in_your_country");

        checkoutRef(branchName);
        copyRepoToFolder(actualFolder.getRoot().toPath());

        checkFoldersEqual();
    }

    @Test
    public void testCheckoutRevision() throws IOException {
        String masterBranch = storage.readCurBranchName();

        commitFile("1.txt", "1");
        commitFile("fold/2.txt", "2");
        commitFile("fold/fold/3.txt", "3");

        String ref = storage.readBranch(masterBranch).getHeadCommitId().toString();
        copyRepoToFolder(expectedFolder.getRoot().toPath());

        commitFile("4.txt", "4");
        commitFile("fold1/5.txt", "5");
        commitFile("fold/fold/6.txt", "6");

        checkoutRef(ref);
        copyRepoToFolder(actualFolder.getRoot().toPath());


        checkFoldersEqual();
    }
}
