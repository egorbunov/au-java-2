package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.storage.io.StoreUtils;
import ru.spbau.mit.java.wit.storage.pack.*;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

/*
  Created by: Egor Gorbunov
  Date: 9/29/16
  Email: egor-mailbox@ya.com
 */

/**
 * Class, which provides read/write methods
 * for every repository object/file.
 *
 * Every method may produce {@link StorageException}
 */
public class WitStorage {
    private final Path witRoot;

    public WitStorage(Path witRoot) {
        this.witRoot = witRoot;
    }

    public static class StorageException extends IOError {
        public StorageException(Throwable cause) {
            super(cause);
        }
    }

    public Path getWitRoot() {
        return witRoot;
    }

    // branches
    public void writeBranch(Branch b) {
        try {
            StoreUtils.write(
                    b,
                    WitPaths.getBranchInfoFilePath(witRoot, b.getName()),
                    BranchStore::pack
            );
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Returns Branch if exists, else null
     */
    public Branch readBranch(String branchName) {
        if (Files.notExists(WitPaths.getBranchInfoFilePath(witRoot, branchName))) {
            return null;
        }
        try {
            return StoreUtils.read(
                    WitPaths.getBranchInfoFilePath(witRoot, branchName),
                    BranchStore::unpack
            );
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public String readCurBranchName() {
        try {
            return StoreUtils.read(WitPaths.getCurBranchNameFilePath(witRoot),
                    StoreUtils::stringUnpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public void writeCurBranchName(String branchName) {
        try {
            StoreUtils.write(branchName, WitPaths.getCurBranchNameFilePath(witRoot),
                    StoreUtils::stringPack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    // commits
    public ShaId writeCommit(Commit c) {
        try {
            return StoreUtils.writeSha(c, WitPaths.getCommitsDir(witRoot),
                    CommitStore::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Return commit is exists, else null
     */
    public Commit readCommit(ShaId id) {
        if (Files.notExists(WitPaths.getCommitsDir(witRoot).resolve(id.toString()))) {
            return null;
        }
        try {
            return StoreUtils.read(WitPaths.getCommitsDir(witRoot).resolve(id.toString()),
                    CommitStore::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public List<ShaId> resolveCommitIdsByPrefix(String prefix) {
        try {
            return Files.list(WitPaths.getCommitsDir(witRoot)).map(Path::getFileName)
                    .map(Path::toString).filter(s -> s.startsWith(prefix))
                    .map(ShaId::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    // snapshots
    public ShaId writeSnapshot(Snapshot s) {
        try {
            return StoreUtils.writeSha(s, WitPaths.getSnapshotsDir(witRoot),
                    SnapshotStore::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public Snapshot readSnapshot(ShaId id) {
        try {
            return StoreUtils.read(WitPaths.getSnapshotsDir(witRoot).resolve(id.toString()),
                    SnapshotStore::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    // blob files
    public ShaId writeBlob(File f) {
        try {
            return StoreUtils.writeSha(f, WitPaths.getBlobsDir(witRoot),
                    StoreUtils::filePack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public Path getBlobFile(ShaId id) {
        return WitPaths.getBlobsDir(witRoot).resolve(id.toString());
    }

    public void checkoutBlob(ShaId blobId, Path pToCheckout) {
        try {
            Files.copy(getBlobFile(blobId), pToCheckout, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    // index
    public void writeIndex(Index index) {
        try {
            StoreUtils.write(index, WitPaths.getIndexFilePath(witRoot), IndexStore::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public Index readIndex() {
        try {
            return StoreUtils.read(WitPaths.getIndexFilePath(witRoot), IndexStore::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    // log
    public void writeLog(Log log, String branch) {
        try {
            StoreUtils.write(log, WitPaths.getLogPath(witRoot, branch), LogStore::pack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    public Log readLog(String branch) {
        try {
            return StoreUtils.read(WitPaths.getLogPath(witRoot, branch), LogStore::unpack);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }
}
