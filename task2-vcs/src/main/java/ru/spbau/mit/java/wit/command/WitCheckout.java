package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.log.Logging;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.Snapshot;
import ru.spbau.mit.java.wit.model.id.ShaId;
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

        if (checkOutBranch != null) {
            commitId = checkOutBranch.getHeadCommitId();
        } else {
            List<ShaId> ids = storage.resolveCommitIdsByPrefix(ref);
            if (ids.size() > 1) {
                System.err.println("Error: can't resolve commit, " +
                        "too short prefix: " + ref);
                return -1;
            } else if (ids.isEmpty()) {
                System.err.println("Error: can't find commit or branch!");
                return -1;
            }
            commitId = ids.get(0);
        }

        if (storage.readMergeFlag() != null) {
            System.err.println("Error: finish merging before checkout");
            return -1;
        }

        Index curIndex = storage.readIndex();
        if (!WitUtils.getStagedEntries(curIndex).findAny().equals(Optional.empty())) {
            System.err.println("Error: you have staged changes in your repository, " +
                    "commit them before checking out.");
            return -1;
        }

        Index newIndex = new Index();

        Snapshot snapshot;
        Commit commit = storage.readCommit(commitId);
        snapshot = storage.readSnapshot(commit.getSnapshotId());

        // checking out and filling index
        for (Snapshot.Entry entry : snapshot) {
            String name = entry.fileName;
            Path pToCheckout = userRepositoryPath.resolve(name);

            storage.checkoutBlob(entry.id, pToCheckout);

            newIndex.add(new Index.Entry(
                    name, pToCheckout.toFile().lastModified(), entry.id,
                    entry.id));
        }

        // deleting files not existing in target revision
        for (Index.Entry entry : curIndex) {
            // delete if file is not in target revision
            if (snapshot.getBlobIdByFileName(entry.fileName) == null) {
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

        // replacing index
        storage.writeIndex(newIndex);

        if (checkOutBranch == null) {
            logger.info("Creating detached branch");
            // checking out by commit ref => need to create new detached branch
            // and fill log properly for that branch
            Branch detachBranch = new Branch("detach_".concat(commitId.toString()), commitId);
            List<ShaId> log = WitUtils.getCommitHistory(commitId, storage).map(it -> it.id)
                    .collect(Collectors.toList());

            storage.writeBranch(detachBranch);
            storage.writeCommitLog(log, detachBranch.getName());
            storage.writeCurBranchName(detachBranch.getName());

            System.out.println("Switched to revision " + ref);
        } else {
            logger.info("Switching to branch " + checkOutBranch.getName());
            storage.writeCurBranchName(checkOutBranch.getName());

            System.out.println("Switched to branch " + ref);
        }
        return 0;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
