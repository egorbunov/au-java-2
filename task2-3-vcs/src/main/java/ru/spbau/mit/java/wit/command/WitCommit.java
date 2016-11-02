package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import ru.spbau.mit.java.wit.command.except.MessageIsEmpty;
import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitLogUtils;
import ru.spbau.mit.java.wit.repository.WitStatusUtils;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "commit", description = "WitCommit snapshot to vcs")
public class WitCommit implements WitCommand {
    @Option(name = "-m", description = "Inline message")
    private String msg;

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        if (msg.isEmpty()) {
            throw new MessageIsEmpty();
        }
        Index index = storage.readIndex();
        List<Index.Entry> deleted = WitStatusUtils.getStagedDeleted(index).collect(Collectors.toList());
        List<Index.Entry> modified = WitStatusUtils.getStagedModified(index).collect(Collectors.toList());
        List<Index.Entry> added = WitStatusUtils.getStagedNew(index).collect(Collectors.toList());
        if (deleted.isEmpty() && modified.isEmpty() && added.isEmpty()) {
            System.out.println("Noting to commit...");
            return 0;
        }
        updateIndex(index, deleted, modified, added);

        Snapshot newSnapshot = index.stream()
                .map(e -> new Snapshot.Entry(e.curBlobId, e.fileName))
                .collect(Collectors.toCollection(Snapshot::new));

        ShaId treeId = storage.writeSnapshot(newSnapshot);
        Branch curBranch = storage.readBranch(storage.readCurBranchName());
        Commit commit = prepareCommit(treeId, storage, curBranch);
        ShaId commitId = storage.writeCommit(commit);

        List<ShaId> log = prepareLog(storage, curBranch, commit, commitId);
        storage.writeCommitLog(log, curBranch.getName());

        // Updating branch
        curBranch.setHeadCommitId(commitId);
        storage.writeBranch(curBranch);
        storage.writeIndex(index);

        if (commit.getParentCommitsIds().size() > 1) {
            storage.writeMergeFlag(null);
            System.out.println("Merge Finished");
        }

        // providing info to user
        Path witRoot = storage.getWitRoot();
        printCommitInfo(witRoot, workingDir, curBranch, deleted, modified, added);
        return 0;
    }

    private List<ShaId> prepareLog(WitStorage storage, Branch curBranch, Commit commit, ShaId commitId) throws IOException {
        List<ShaId> log;
        if (commit.getParentCommitsIds().size() > 1) {
            log = storage.readCommitLog(curBranch.getName());
            log.add(commitId);
        } else {
            // need to merge logs in case merge was performed
            log = WitLogUtils.readCommitHistory(commitId, storage).map(it -> it.id).collect(Collectors.toList());
        }
        return log;
    }

    private void printCommitInfo(Path witRoot, Path workingDir, Branch curBranch, List<Index.Entry> deleted, List<Index.Entry> modified, List<Index.Entry> added) {
        Path userRepositoryPath = WitUtils.stripWitStoragePath(witRoot);

        System.out.println("On branch [ " + curBranch.getName() + " ]");
        System.out.println("    " + modified.size() + " files modified; " +
                                    added.size() + " files added; " +
                                    deleted.size() + " files deleted");
        for (Index.Entry e : deleted) {
            System.out.println("    delete " +
                    workingDir.relativize(userRepositoryPath.resolve(e.fileName)));
        }
        for (Index.Entry e : added) {
            System.out.println("    add    " +
                    workingDir.relativize(userRepositoryPath.resolve(e.fileName)));
        }
    }

    private Commit prepareCommit(ShaId snapshotId, WitStorage storage, Branch curBranch) throws IOException {

        List<ShaId> parents = new ArrayList<>();
        parents.add(curBranch.getHeadCommitId());
        // if merging stage --> need to set more parent commits
        String mergingBranch = storage.readMergeFlag();
        if (mergingBranch != null) {
            parents.add(storage.readBranch(mergingBranch).getHeadCommitId());
        }
        return new Commit(parents, snapshotId, msg, System.currentTimeMillis());
    }

    private static void updateIndex(Index index,
                                    List<Index.Entry> deleted,
                                    List<Index.Entry> modified,
                                    List<Index.Entry> added) {
        deleted.forEach(index::remove);
        Stream.concat(modified.stream(), added.stream())
                .forEach(index::remove);
        Stream.concat(modified.stream(), added.stream())
                .forEach(it -> index.add(new Index.Entry(
                        it.fileName,
                        it.modified,
                        it.curBlobId,
                        it.curBlobId)));
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
