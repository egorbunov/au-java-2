package ru.spbau.mit.java.wit.command;

import com.google.common.collect.Lists;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.nio.file.Path;
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
    public String msg;

    @Override
    public int execute(Path workingDir, WitStorage storage) {
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
                        it.lastModified,
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
        commit.setParentCommitsIds(Lists.newArrayList(curBranch.getHeadCommitId()));
        commit.setSnapshotId(treeId);
        commit.setTimestamp(System.currentTimeMillis());


        ShaId commitId;
        commitId = storage.writeCommit(commit);

        // Updating branch
        curBranch.setHeadCommitId(commitId);

        // Storing changed index
        storage.writeIndex(index);

        // Update log
        List<ShaId> log = storage.readCommitLog(curBranch.getName());
        log.add(commitId);
        storage.writeCommitLog(log, curBranch.getName());

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
}
