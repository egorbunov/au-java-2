package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
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
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "checkout", description = "Switch to branch or revision")
public class WitCheckout implements WitCommand {
    @Arguments(description = "Name of branch or revision (commit) identifier")
    String ref;

    @Override
    public int execute(Path workingDir, WitStorage storage) {
        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());

        // resolving commit reference
        ShaId commitId;
        Branch b = storage.readBranch(ref);
        boolean checkoutDetach = false;
        if (b != null) {
            commitId = b.getHeadCommitId();
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

            checkoutDetach = true;
        }

        // checking out and creating new index
        Index newIndex = new Index();

        Snapshot snapshot;
        Commit commit = storage.readCommit(commitId);
        snapshot = storage.readSnapshot(commit.getSnapshotId());

        for (Snapshot.Entry entry : snapshot) {
            String name = entry.fileName;
            Path pToCheckout = userRepositoryPath.resolve(name);

            // TODO: do not copy if files are equal
            storage.checkoutBlob(entry.id, pToCheckout);

            // index update...
            newIndex.add(new Index.Entry(
                    name, pToCheckout.toFile().lastModified(), entry.id,
                    entry.id));
        }

        // deleting files not existing in target revision
        Index curIndex;
        curIndex = storage.readIndex();

        for (Index.Entry e : curIndex) {
            // deleting not in target revision && only if file is commited
            if (snapshot.getBlobIdByFileName(e.fileName) == null &&
                    e.curBlobId.equals(e.lastCommitedBlobId)) {

                Path p = userRepositoryPath.resolve(e.fileName);
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e1) {
                    System.err.println("Error: can't delete file " + p);
                    return -1;
                }
            }
        }

        // replacing index
        storage.writeIndex(newIndex);

        if (checkoutDetach) {
            // checking out by commit ref => need to create new detached branch
            // and fill log properly for that branch

            Branch detachBranch = new Branch("detach_".concat(commitId.toString()), commitId);
            List<ShaId> log = WitUtils.getCommitHistory(commitId, storage).map(it -> it.id)
                    .collect(Collectors.toList());

            storage.writeBranch(detachBranch);
            storage.writeCommitLog(log, detachBranch.getName());
            storage.writeCurBranchName(detachBranch.getName());
        }
        return 0;
    }
}
