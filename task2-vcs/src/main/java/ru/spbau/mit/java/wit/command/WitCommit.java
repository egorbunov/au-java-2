package ru.spbau.mit.java.wit.command;

import com.google.common.collect.Lists;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
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
            System.err.println("Error: Empty message");
            return -1;
        }

        // Creating new Snapshot tree and new Index
        Index index;
        index = storage.readIndex();

        List<Index.Entry> deleted = WitUtils.getStagedDeleted(index).collect(Collectors.toList());
        List<Index.Entry> modified = WitUtils.getStagedModified(index).collect(Collectors.toList());
        List<Index.Entry> added = WitUtils.getStagedNew(index).collect(Collectors.toList());
        if (deleted.isEmpty() && modified.isEmpty() && added.isEmpty()) {
            // if there was no files staged, there is no reason to commit!
            System.out.println("Noting to commit...");
            return 0;
        }

        deleted.forEach(index::remove);
        Stream.concat(modified.stream(), added.stream())
                .forEach(index::remove);
        // due to commit we updating last commit blob id field
        Stream.concat(modified.stream(), added.stream())
                .forEach(it -> index.add(new Index.Entry(
                        it.fileName,
                        it.modified,
                        it.curBlobId,
                        it.curBlobId)));

        Snapshot newSnapshot = index.stream()
                .map(e -> new Snapshot.Entry(e.curBlobId, e.fileName))
                .collect(Collectors.toCollection(Snapshot::new));

        // Storing new snapshot
        ShaId treeId;
        treeId = storage.writeSnapshot(newSnapshot);

        // reading current branch, cause we 
        Branch curBranch;
        String curBranchName = storage.readCurBranchName();
        curBranch = storage.readBranch(curBranchName);


        Commit commit = new Commit();
        commit.setMsg(msg);
        commit.setSnapshotId(treeId);
        commit.setTimestamp(System.currentTimeMillis());

        List<ShaId> parents = new ArrayList<>();
        parents.add(curBranch.getHeadCommitId());
        // if merging stage --> need to set more parent commits
        String mergingBranch = storage.readMergeFlag();
        if (mergingBranch != null) {
            parents.add(storage.readBranch(mergingBranch).getHeadCommitId());
        }
        commit.setParentCommitsIds(parents);

        // write commit
        ShaId commitId;
        commitId = storage.writeCommit(commit);

        // Update log
        List<ShaId> log;
        if (mergingBranch == null) {
            log = storage.readCommitLog(curBranch.getName());
            log.add(commitId);
        } else {
            // need to merge logs in case merge was performed
            log = WitUtils.getCommitHistory(commitId, storage).map(it -> it.id).collect(Collectors.toList());
        }
        storage.writeCommitLog(log, curBranch.getName());

        // Updating branch
        curBranch.setHeadCommitId(commitId);
        storage.writeBranch(curBranch);

        // Storing changed index
        storage.writeIndex(index);

        // Finish merging if needed
        if (mergingBranch != null) {
            storage.writeMergeFlag(null);
            System.out.println("Merge Finished");
        }


        // providing info to user
        Path witRoot = storage.getWitRoot();
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

        return 0;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
