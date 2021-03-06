package ru.spbau.mit.java.wit.test;

import org.apache.commons.codec.digest.Sha2Crypt;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitInit;
import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
public class StorageTest {
    private static WitStorage storage;
    private static TemporaryFolder folder;

    @BeforeClass
    public static void setup() throws IOException {
        folder = new TemporaryFolder();
        folder.create();
        WitInit init = new WitInit();
        init.execute(folder.getRoot().toPath(), null);
        Path witRoot = WitInit.findRepositoryRoot(folder.getRoot().toPath());
        storage = new WitStorage(witRoot);
    }

    @AfterClass
    public static void destroy() {
        folder.delete();
    }

    @Test
    public void testWriteBranch() throws IOException {
        Branch b = new Branch("new_branch", ShaId.create("y"));
        storage.writeBranch(b);
        Branch actualBranch = storage.readBranch(b.getName());

        Assert.assertEquals(b.getName(), actualBranch.getName());
        Assert.assertEquals(b.getHeadCommitId(), actualBranch.getHeadCommitId());
    }

    @Test
    public void testWriteReadIndex() throws IOException {
        // empty index read must be ok
        Index index = storage.readIndex();
        Assert.assertEquals(index.size(), 0);


        index = new Index();
        index.add(new Index.Entry("filetxt", 123456, ShaId.create("a"), ShaId.create("b")));
        index.add(new Index.Entry("txtfile", 654321, ShaId.create("c"), ShaId.create("d")));

        storage.writeIndex(index);
        Index actual = storage.readIndex();

        Assert.assertEquals(index.size(), actual.size());
        Assert.assertArrayEquals(
                index.stream().collect(Collectors.toList()).toArray(),
                actual.stream().collect(Collectors.toList()).toArray()
        );
    }

    @Test
    public void testWriteCommit() throws IOException {
        Commit commit = new Commit();
        commit.setMsg("msg");
        commit.setParentCommitsIds(Arrays.asList(ShaId.create("1"), ShaId.create("2"), ShaId.create("3")));
        commit.setSnapshotId(ShaId.create("123sd12312"));
        commit.setTimestamp(System.currentTimeMillis());

        ShaId id = storage.writeCommit(commit);
        Commit actual = storage.readCommit(id);

        Assert.assertEquals(commit.getMsg(), actual.getMsg());
        Assert.assertArrayEquals(commit.getParentCommitsIds().toArray(),
                actual.getParentCommitsIds().toArray());
        Assert.assertEquals(commit.getSnapshotId(), actual.getSnapshotId());
    }

    @Test
    public void testWriteSnapshot() throws IOException {
        Snapshot snapshot = new Snapshot();
        snapshot.add(new Snapshot.Entry(ShaId.create("1"), "file"));
        snapshot.add(new Snapshot.Entry(ShaId.create("2"), "file1"));

        ShaId id = storage.writeSnapshot(snapshot);
        Snapshot actual = storage.readSnapshot(id);

        Assert.assertEquals(snapshot.size(), actual.size());
        Assert.assertArrayEquals(
                snapshot.stream().collect(Collectors.toList()).toArray(),
                actual.stream().collect(Collectors.toList()).toArray()
        );
    }

    @Test
    public void testCurBranch() throws IOException {
        final String b = "super";
        storage.writeCurBranchName(b);
        Assert.assertEquals(b, storage.readCurBranchName());
    }

    @Test
    public void testBlobFileWrite() throws IOException {
        String fileName = "test_file.txt";
        File f = folder.newFile(fileName);
        FileUtils.writeLines(f,
                Arrays.asList("int main(void) {", " return 0;", "}"));

        ShaId fileId = storage.writeBlob(f);
        File actualFile = storage.getBlobFile(fileId).toFile();

        Assert.assertEquals(
                FileUtils.readFileToString(f, Charset.defaultCharset()),
                FileUtils.readFileToString(actualFile, Charset.defaultCharset())
        );
    }

    @Test
    public void testLogWrite() throws IOException {
        String branchName = "branch";
        List<ShaId> log = new ArrayList<>();
        log.add(ShaId.create("this_is_id0x35"));
        storage.writeCommitLog(log, branchName);

        List<ShaId> actual = storage.readCommitLog(branchName);

        Assert.assertEquals(log.size(), actual.size());
        Assert.assertArrayEquals(
                log.toArray(),
                actual.toArray()
        );
    }
}
