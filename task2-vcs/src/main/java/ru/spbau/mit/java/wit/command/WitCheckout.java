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
    private Logger logger = Logging.getLogger(WitCheckout.class.getName());

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
            List<ShaId> cmts = storage.resolveCommitIdsByPrefix(ref);
            if (cmts.size() > 1) {
                System.err.println("Error: can't resolve commit, " +
                        "too short prefix: " + ref);
                return -1;
            } else if (cmts.isEmpty()) {
                System.err.println("Error: can't find commit or branch!");
                return -1;
            }
            commitId = cmts.get(0);
        }

        // checking out and creating new index
        Index newIndex = new Index();

        Snapshot snapshot;
        Commit commit = storage.readCommit(commitId);
        snapshot = storage.readSnapshot(commit.getSnapshotId());

        for (Snapshot.Entry entry : snapshot) {
            String name = entry.fileName;
            Path pToCheckout = userRepositoryPath.resolve(name);

            // TODO: do not copy if files are equal if possible
            storage.checkoutBlob(entry.id, pToCheckout);

            // index update...
            newIndex.add(new Index.Entry(
                    name, pToCheckout.toFile().lastModified(), entry.id,
                    entry.id));
        }

        // deleting files not existing in target revision
        Index curIndex = storage.readIndex();
        for (Index.Entry entry : curIndex) {
            // deleting not in target revision && only if file is committed
            if (snapshot.getBlobIdByFileName(entry.fileName) == null
                    && entry.curBlobId.equals(entry.lastCommitedBlobId)) {
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
        } else {
            logger.info("Switching to branch " + checkOutBranch.getName());
            storage.writeCurBranchName(checkOutBranch.getName());
        }
        return 0;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
