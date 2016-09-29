package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.storage.io.StoreUtils;
import ru.spbau.mit.java.wit.storage.pack.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */

/**
 * Class, which provides read/write methods
 * for every repository object/file.
 */
public class WitStorage {
    private Path witRoot;

    public WitStorage(Path witRoot) {
        this.witRoot = witRoot;
    }

    public void writeBranch(Branch b) throws IOException {
        StoreUtils.write(
                b,
                WitPaths.getBranchInfoFilePath(witRoot, b.getName()),
                BranchStore::pack
        );
    }

    public Branch readBranch(String branchName) throws IOException {
        return StoreUtils.read(
                WitPaths.getBranchInfoFilePath(witRoot, branchName),
                BranchStore::unpack
        );
    }


    public ShaId writeCommit(Commit c) throws IOException {
        return StoreUtils.writeSha(c, WitPaths.getCommitsDir(witRoot),
                CommitStore::pack);
    }

    public Commit readCommit(ShaId id) throws IOException {
        return StoreUtils.read(WitPaths.getCommitsDir(witRoot).resolve(id.toString()),
                CommitStore::unpack);
    }

    public List<ShaId> resolveCommitIdsByPrefix(String prefix) throws IOException {
        return Files.list(WitPaths.getCommitsDir(witRoot)).map(Path::getFileName)
                .map(Path::toString).filter(s -> s.startsWith(prefix))
                .map(ShaId::new)
                .collect(Collectors.toList());
    }

    public ShaId writeSnapshot(Snapshot s) throws IOException {
        return StoreUtils.writeSha(s, WitPaths.getSnapshotsDir(witRoot),
                SnapshotStore::pack);
    }

    public Snapshot readSnapshot(ShaId id) throws IOException {
        return StoreUtils.read(WitPaths.getSnapshotsDir(witRoot).resolve(id.toString()),
                SnapshotStore::unpack);
    }

    public ShaId writeBlob(File f) throws IOException {
        return StoreUtils.writeSha(f, WitPaths.getBlobsDir(witRoot),
                StoreUtils::filePack);
    }

    public Path getBlobFile(ShaId id) {
        return WitPaths.getBlobsDir(witRoot).resolve(id.toString());
    }

    public void checkoutBlob(ShaId blobId, Path pToCheckout) throws IOException {
        Files.copy(getBlobFile(blobId), pToCheckout, StandardCopyOption.REPLACE_EXISTING);
    }

    public void writeIndex(Index index) throws IOException {
        StoreUtils.write(index, WitPaths.getIndexFilePath(witRoot), IndexStore::pack);
    }

    public Index readIndex() throws IOException {
        return StoreUtils.read(WitPaths.getIndexFilePath(witRoot), IndexStore::unpack);
    }

    public String readCurBranchName() throws IOException {
        return StoreUtils.read(WitPaths.getCurBranchNameFilePath(witRoot),
                StoreUtils::stringUnpack);
    }

    public void writeCurBranchName(String branchName) throws IOException {
        StoreUtils.write(branchName, WitPaths.getCurBranchNameFilePath(witRoot),
                StoreUtils::stringPack);
    }

    public void writeLog(Log log, String branch) throws IOException {
        StoreUtils.write(log, WitPaths.getLogPath(witRoot, branch), LogStore::pack);
    }

    public Log readLog(String branch) throws IOException {
        return StoreUtils.read(WitPaths.getLogPath(witRoot, branch), LogStore::unpack);
    }
}
