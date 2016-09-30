package ru.spbau.mit.java.wit.repository.storage;

import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.pack.*;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
 *
 * It aimed to encapsulate all i/o with repository storage
 *
 * Every method may produce {@link StorageException}
 *
 * TODO: generalize interfaces?
 */
public class WitStorage {
    /**
     * IOException wrapper for every IO excpetion occurred during storage
     * io operations
     */
    public static class StorageException extends IOError {
        StorageException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * List of dirs and files, which designate wit repo storage folder
     */
    private static List<Path> witStorageDirsSignature = Arrays.asList(
            WitStoragePaths.getBlobsDir(Paths.get("")),
            WitStoragePaths.getCommitsDir(Paths.get("")),
            WitStoragePaths.getSnapshotsDir(Paths.get("")),
            WitStoragePaths.getBranchesDirPath(Paths.get("")),
            WitStoragePaths.getLogDir(Paths.get(""))
    );
    private static List<Path> witStorageFilesSignature = Arrays.asList(
            WitStoragePaths.getIndexFilePath(Paths.get("")),
            WitStoragePaths.getCurBranchNameFilePath(Paths.get("")),
            WitStoragePaths.getBranchInfoFilePath(Paths.get(""), "master")
    );

    /**
     * Root of the storage - directory, which resides at root user
     * working directory; It keeps all service data and history
     */
    private final Path witRoot;

    public WitStorage(Path witRoot) {
        this.witRoot = witRoot;
    }

    /**
     * Creates directory and file structure, which needed for
     * work with repository storage;
     */
    public void createStorageStructure() {
        try {
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
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public boolean isValidStorageStructure() {
        return Stream.concat(
                witStorageDirsSignature.stream(),
                witStorageFilesSignature.stream()
        ).map(witRoot::resolve).allMatch(Files::exists);
    }



    public Path getWitRoot() {
        return witRoot;
    }

    public void writeBranch(Branch b) {
        try {
            StoreUtils.write(
                    b,
                    WitStoragePaths.getBranchInfoFilePath(witRoot, b.getName()),
                    BranchPack::pack
            );
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Returns Branch if exists, else null
     */
    public Branch readBranch(String branchName) {
        if (Files.notExists(WitStoragePaths.getBranchInfoFilePath(witRoot, branchName))) {
            return null;
        }
        try {
            return StoreUtils.read(
                    WitStoragePaths.getBranchInfoFilePath(witRoot, branchName),
                    BranchPack::unpack
            );
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public String readCurBranchName() {
        try {
            return StoreUtils.read(WitStoragePaths.getCurBranchNameFilePath(witRoot),
                    StoreUtils::stringUnpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public void writeCurBranchName(String branchName) {
        try {
            StoreUtils.write(branchName, WitStoragePaths.getCurBranchNameFilePath(witRoot),
                    StoreUtils::stringPack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public List<Branch> readAllBranches() {
        try {
            return Files.walk(WitStoragePaths.getBranchesDirPath(witRoot))
                    .map(p -> readBranch(p.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public ShaId writeCommit(Commit c) {
        try {
            return StoreUtils.writeSha(c, WitStoragePaths.getCommitsDir(witRoot),
                    CommitPack::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Return commit is exists, else null
     */
    public Commit readCommit(ShaId id) {
        if (Files.notExists(WitStoragePaths.getCommitsDir(witRoot).resolve(id.toString()))) {
            return null;
        }
        try {
            return StoreUtils.read(WitStoragePaths.getCommitsDir(witRoot).resolve(id.toString()),
                    CommitPack::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public List<ShaId> resolveCommitIdsByPrefix(String prefix) {
        try {
            return Files.list(WitStoragePaths.getCommitsDir(witRoot)).map(Path::getFileName)
                    .map(Path::toString).filter(s -> s.startsWith(prefix))
                    .map(ShaId::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public ShaId writeSnapshot(Snapshot s) {
        try {
            return StoreUtils.writeSha(s, WitStoragePaths.getSnapshotsDir(witRoot),
                    SnapshotPack::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public Snapshot readSnapshot(ShaId id) {
        try {
            return StoreUtils.read(WitStoragePaths.getSnapshotsDir(witRoot).resolve(id.toString()),
                    SnapshotPack::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public ShaId writeBlob(File f) {
        try {
            return StoreUtils.writeSha(f, WitStoragePaths.getBlobsDir(witRoot),
                    StoreUtils::filePack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public Path getBlobFile(ShaId id) {
        return WitStoragePaths.getBlobsDir(witRoot).resolve(id.toString());
    }

    public void checkoutBlob(ShaId blobId, Path pToCheckout) {
        try {
            Files.copy(getBlobFile(blobId), pToCheckout, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public void writeIndex(Index index) {
        try {
            StoreUtils.write(index, WitStoragePaths.getIndexFilePath(witRoot), IndexPack::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public Index readIndex() {
        try {
            return StoreUtils.read(WitStoragePaths.getIndexFilePath(witRoot), IndexPack::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     *
     * @param log list of commit ids
     * @param branch branch name, which log is written
     */
    public void writeCommitLog(List<ShaId> log, String branch) {
        try {
            StoreUtils.write(log, WitStoragePaths.getLogPath(witRoot, branch), IdListPack::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Reads commit list for given branch; Returns {@code null} if there is
     * not log file for given branch name
     */
    public List<ShaId> readCommitLog(String branch) {
        if (Files.notExists(WitStoragePaths.getLogPath(witRoot, branch))) {
            return null;
        }
        try {
            return StoreUtils.read(WitStoragePaths.getLogPath(witRoot, branch), IdListPack::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }
}
