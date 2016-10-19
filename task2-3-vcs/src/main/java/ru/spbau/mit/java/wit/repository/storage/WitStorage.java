package ru.spbau.mit.java.wit.repository.storage;

import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.pack.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
  Created by: Egor Gorbunov
  Date: 9/29/16
  Email: egor-mailbox@ya.com
 */

/**
 * Class, which provides read/write methods
 * for every repository object/file.
 * <p>
 * It aimed to encapsulate all i/o operations with repository storage
 * <p>
 * TODO: generalize interfaces?
 */
public class WitStorage {
    /**
     * List of dirs and files, which designate wit repo storage folder
     */
    private static final List<Path> witStorageDirsSignature = Arrays.asList(
            WitStoragePaths.getBlobsDir(Paths.get("")),
            WitStoragePaths.getCommitsDir(Paths.get("")),
            WitStoragePaths.getSnapshotsDir(Paths.get("")),
            WitStoragePaths.getBranchesDirPath(Paths.get("")),
            WitStoragePaths.getLogDir(Paths.get(""))
    );
    private static final List<Path> witStorageFilesSignature = Arrays.asList(
            WitStoragePaths.getIndexFilePath(Paths.get("")),
            WitStoragePaths.getCurBranchNameFilePath(Paths.get("")),
            WitStoragePaths.getBranchInfoFilePath(Paths.get(""), "master")
    );

    /**
     * Creates directory and file structure, which needed for
     * work with repository storage;
     */
    public static void createStorageStructure(Path witRoot) throws IOException {
        Files.createDirectory(witRoot);
        for (Path p : witStorageDirsSignature) {
            if (Files.notExists(witRoot.resolve(p))) {
                Files.createDirectories(witRoot.resolve(p));
            }
        }
        for (Path p : witStorageFilesSignature) {
            if (Files.notExists(witRoot.resolve(p))) {
                Files.createFile(witRoot.resolve(p));
            }
        }
    }

    public static boolean isValidStorageStructure(Path witRoot) {
        return Stream.concat(
                witStorageDirsSignature.stream(),
                witStorageFilesSignature.stream()
        ).map(witRoot::resolve).allMatch(Files::exists);
    }

    /**
     * Root of the storage - directory, which resides at root user
     * working directory; It keeps all service data and history
     */
    private final Path witRoot;

    public WitStorage(Path witRoot) {
        this.witRoot = witRoot;
    }

    public Path getWitRoot() {
        return witRoot;
    }

    public void writeBranch(Branch b) throws IOException {
        StoreUtils.write(
                b,
                WitStoragePaths.getBranchInfoFilePath(witRoot, b.getName()),
                BranchPack::pack
        );
    }

    /**
     * Returns Branch if exists, else null
     */
    public Branch readBranch(String branchName) throws IOException {
        if (Files.notExists(WitStoragePaths.getBranchInfoFilePath(witRoot, branchName))) {
            return null;
        }
        return StoreUtils.read(
                WitStoragePaths.getBranchInfoFilePath(witRoot, branchName),
                BranchPack::unpack
        );
    }

    public String readCurBranchName() throws IOException {
        return StoreUtils.read(WitStoragePaths.getCurBranchNameFilePath(witRoot),
                StoreUtils::stringUnpack);
    }

    public void writeCurBranchName(String branchName) throws IOException {
        StoreUtils.write(branchName, WitStoragePaths.getCurBranchNameFilePath(witRoot),
                StoreUtils::stringPack);
    }

    public List<Branch> readAllBranches() throws IOException {
        ArrayList<Branch> branches = new ArrayList<>();
        for (Path p : Files.list(WitStoragePaths.getBranchesDirPath(witRoot))
                .collect(Collectors.toList())) {
            branches.add(readBranch(p.toString()));
        }
        return branches;
    }

    public ShaId writeCommit(Commit c) throws IOException {
        return StoreUtils.writeSha(c, WitStoragePaths.getCommitsDir(witRoot),
                CommitPack::pack);
    }

    /**
     * @return null if there is no commit with given id, else commit object is returned
     */
    public Commit readCommit(ShaId id) throws IOException {
        if (Files.notExists(WitStoragePaths.getCommitsDir(witRoot).resolve(id.toString()))) {
            return null;
        }
        return StoreUtils.read(WitStoragePaths.getCommitsDir(witRoot).resolve(id.toString()),
                CommitPack::unpack);
    }

    public List<ShaId> resolveCommitIdsByPrefix(String prefix) throws IOException {
        return Files.list(WitStoragePaths.getCommitsDir(witRoot)).map(Path::getFileName)
                .map(Path::toString).filter(s -> s.startsWith(prefix))
                .map(ShaId::create)
                .collect(Collectors.toList());
    }

    public ShaId writeSnapshot(Snapshot s) throws IOException {
        return StoreUtils.writeSha(s, WitStoragePaths.getSnapshotsDir(witRoot),
                SnapshotPack::pack);
    }

    public Snapshot readSnapshot(ShaId id) throws IOException {
        return StoreUtils.read(WitStoragePaths.getSnapshotsDir(witRoot).resolve(id.toString()),
                SnapshotPack::unpack);
    }

    /**
     * Writes blob to storage; Data is read from specified file
     */
    public ShaId writeBlob(File f) throws IOException {
        return StoreUtils.writeSha(f, WitStoragePaths.getBlobsDir(witRoot),
                StoreUtils::filePack);
    }

    public Path getBlobFile(ShaId id) {
        return WitStoragePaths.getBlobsDir(witRoot).resolve(id.toString());
    }

    public void checkoutBlob(ShaId blobId, Path pToCheckout) throws IOException {
        Files.copy(getBlobFile(blobId), pToCheckout, StandardCopyOption.REPLACE_EXISTING);
    }

    public void writeIndex(Index index) throws IOException {
        StoreUtils.write(index, WitStoragePaths.getIndexFilePath(witRoot), IndexPack::pack);
    }

    public Index readIndex() throws IOException {
        return StoreUtils.read(WitStoragePaths.getIndexFilePath(witRoot), IndexPack::unpack);
    }

    /**
     * @param log    list of commit ids
     * @param branch branch name, which log is written
     */
    public void writeCommitLog(List<ShaId> log, String branch) throws IOException {
        StoreUtils.write(log, WitStoragePaths.getLogPath(witRoot, branch), IdListPack::pack);
    }

    /**
     * Reads commit list for given branch; Returns {@code null} if there is
     * not log file for given branch name
     */
    public List<ShaId> readCommitLog(String branch) throws IOException {
        if (Files.notExists(WitStoragePaths.getLogPath(witRoot, branch))) {
            return null;
        }
        return StoreUtils.read(WitStoragePaths.getLogPath(witRoot, branch), IdListPack::unpack);
    }

    /**
     * Merge flag specifies merge stage
     */
    public void writeMergeFlag(String branch) throws IOException {
        if (branch == null) {
            Files.deleteIfExists(WitStoragePaths.getMergeFlagPath(witRoot));
            return;
        }
        StoreUtils.write(branch, WitStoragePaths.getMergeFlagPath(witRoot), StoreUtils::stringPack);
    }

    public String readMergeFlag() throws IOException {
        if (Files.notExists(WitStoragePaths.getMergeFlagPath(witRoot))) {
            return null;
        }
        return StoreUtils.read(WitStoragePaths.getMergeFlagPath(witRoot), StoreUtils::stringUnpack);
    }
}
