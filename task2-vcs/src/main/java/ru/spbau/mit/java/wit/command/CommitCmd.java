package ru.spbau.mit.java.wit.command;

import com.google.common.collect.Lists;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import ru.spbau.mit.java.wit.WitCommand;
import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.storage.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "commit", description = "CommitCmd snapshot to vcs")
public class CommitCmd implements WitCommand {
    @Option(name = "-m", description = "Inline message")
    public String msg;

    @Override
    public int run(Path baseDir, WitStorage storage) {
        if (msg.isEmpty()) {
            System.err.println("Error: Empty message");
            return -1;
        }

        // Creating new Snapshot tree and new Index
        Index index;
        index = storage.readIndex();

        // if there was no files staged, there is no reason to commit!
        List<String> changedFiles = index.stream()
                .filter(e -> !e.curBlobId.equals(e.lastCommitedBlobId))
                .map(e -> e.fileName).collect(Collectors.toList());
        if (changedFiles.isEmpty()) {
            System.out.println("Noting to commit...");
            return 0;
        }

        Index newIndex = new Index();
        Snapshot newSnapshot = new Snapshot();
        for (Index.Entry e : index) {
            newSnapshot.add(new Snapshot.Entry(e.curBlobId, e.fileName));
            // marking blob as commited
            newIndex.add(new Index.Entry(e.curBlobId, e.lastModified,
                    e.fileName, e.curBlobId));
        }

        // Storing new snapshot
        ShaId treeId;
        treeId = storage.writeSnapshot(newSnapshot);

        // reading current branch, cause we 
        Branch curBranch;
        String curBranchName = storage.readCurBranchName();
        curBranch = storage.readBranch(curBranchName);

        Commit commit = new Commit();
        commit.setMsg(msg);
        commit.setParentCommitsIds(Lists.newArrayList(curBranch.getCurCommitId()));
        commit.setSnapshotId(treeId);


        ShaId commitId;
        commitId = storage.writeCommit(commit);

        // Updating branch
        Branch updBranch = new Branch();
        updBranch.setName(curBranchName);
        if (!curBranch.getHeadCommitId().equals(curBranch.getCurCommitId())) {
            // creating new branch if needed
            String brName = "detach_" + curBranch.getCurCommitId();
            updBranch.setName(brName);
        }
        updBranch.setCurCommitId(commitId);
        updBranch.setHeadCommitId(commitId);

        // updating branches data
        storage.writeBranch(updBranch);
        // update branch if needed
        if (!updBranch.getName().equals(curBranch.getName())) {
            storage.writeCurBranchName(updBranch.getName());
        }

        // Storing new Index
        storage.writeIndex(newIndex);

        // Update log
        Log log = storage.readLog(updBranch.getName());
        log.add(new Log.Entry(commitId, msg));
        storage.writeLog(log, updBranch.getName());

        // Writing to user =)
        System.out.println("On branch [ " + updBranch.getName() + " ]");
        System.out.println("    " + changedFiles.size() + " files changed");
        return 0;
    }
}
