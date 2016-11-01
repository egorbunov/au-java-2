package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.command.except.CommitNotFound;
import ru.spbau.mit.java.wit.command.except.MergingNotFinished;
import ru.spbau.mit.java.wit.command.except.NotAllChangesCommitted;
import ru.spbau.mit.java.wit.command.except.TooShortIdPrefix;
import ru.spbau.mit.java.wit.log.Logging;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.Snapshot;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitLogUtils;
import ru.spbau.mit.java.wit.repository.WitStatusUtils;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "checkout", description = "Switch to branch or revision")
public class WitCheckout implements WitCommand {
    private final Logger logger = Logging.getLogger(WitCheckout.class.getName());

    @Arguments(description = "Name of branch or revision (commit) identifier")
    private String ref;

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());
        // resolving commit reference
        ShaId commitId;
        Branch checkOutBranch = storage.readBranch(ref);
        commitId = getCommitId(storage, checkOutBranch);

        if (storage.readMergeFlag() != null) {
            throw new MergingNotFinished();
        }
        Index curIndex = storage.readIndex();
        if (!WitStatusUtils.getStagedEntries(curIndex).findAny().equals(Optional.empty())) {
            throw new NotAllChangesCommitted();
        }

        Commit commit = storage.readCommit(commitId);
        Snapshot snapshot = storage.readSnapshot(commit.getSnapshotId());
        Index newIndex = checkoutSnapshotFiles(storage, userRepositoryPath, snapshot);
        deleteFilesNotInRevision(userRepositoryPath, curIndex, snapshot);

        // replacing index
        storage.writeIndex(newIndex);
        if (checkOutBranch == null) {
            detachBranch(storage, commitId);
            System.out.println("Switched to revision " + ref);
        } else {
            logger.info("Switching to branch " + checkOutBranch.getName());
            storage.writeCurBranchName(checkOutBranch.getName());
            System.out.println("Switched to branch " + ref);
        }
        return 0;
    }

    private Index checkoutSnapshotFiles(WitStorage storage, Path userRepositoryPath, Snapshot snapshot) throws IOException {
        Index newIndex = new Index();
        for (Snapshot.Entry entry : snapshot) {
            String name = entry.fileName;
            Path pToCheckout = userRepositoryPath.resolve(name);
            storage.checkoutBlob(entry.id, pToCheckout);
            newIndex.add(new Index.Entry(
                    name, pToCheckout.toFile().lastModified(), entry.id,
                    entry.id));
        }
        return newIndex;
    }

    private void detachBranch(WitStorage storage, ShaId commitId) throws IOException {
        logger.info("Creating detached branch");
        // checking out by commit ref => need to create new detached branch
        // and fill log properly for that branch
        Branch detachBranch = new Branch("detach_".concat(commitId.toString()), commitId);
        List<ShaId> log = WitLogUtils.readCommitHistory(commitId, storage).map(it -> it.id)
                .collect(Collectors.toList());

        storage.writeBranch(detachBranch);
        storage.writeCommitLog(log, detachBranch.getName());
        storage.writeCurBranchName(detachBranch.getName());
    }

    private ShaId getCommitId(WitStorage storage, Branch checkOutBranch) throws IOException {
        ShaId commitId;
        if (checkOutBranch != null) {
            commitId = checkOutBranch.getHeadCommitId();
        } else {
            List<ShaId> ids = storage.resolveCommitIdsByPrefix(ref);
            if (ids.size() > 1) {
                throw new TooShortIdPrefix(ref);
            } else if (ids.isEmpty()) {
                throw new CommitNotFound(ref);
            }
            commitId = ids.get(0);
        }
        return commitId;
    }

    private void deleteFilesNotInRevision(Path userRepositoryPath,
                                          Index curIndex,
                                          Snapshot targetRevision) throws IOException {
        for (Index.Entry entry : curIndex) {
            // delete if file is not in target revision
            if (targetRevision.getBlobIdByFileName(entry.fileName) == null) {
                Path p = userRepositoryPath.resolve(entry.fileName);
                Files.deleteIfExists(p);

                // erasing empty parent directories, because we can!
                Path parent = p.getParent();
                while (Files.list(parent).findFirst().equals(Optional.empty())) {
                    Files.delete(parent);
                    parent = parent.getParent();
                }
            }
        }
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
