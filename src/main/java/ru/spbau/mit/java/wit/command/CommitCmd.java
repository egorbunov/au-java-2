package ru.spbau.mit.java.wit.command;

import com.google.common.collect.Lists;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.storage.*;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "commit", description = "CommitCmd snapshot to vcs")
public class CommitCmd implements Runnable {
    @Option(name = "-m", description = "Inline message")
    String msg;

    @Override
    public void run() {
        if (WitRepo.findRepositoryRoot() == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        if (msg.isEmpty()) {
            System.err.println("Error: Empty message");
            return;
        }

        // Creating new Snapshot tree and new Index
        Index index = IndexStorage.readIndex();
        Index newIndex = new Index();
        SnapshotTree newSnapshot = new SnapshotTree();
        for (Index.Entry e : index.getEntries()) {
            newSnapshot.putFile(e.curBlobId, e.fileName);
            // marking blob as commited
            newIndex.addEntry(new Index.Entry(e.curBlobId, e.lastModified,
                    e.fileName, e.curBlobId));
        }

        // Storing new snapshot
        ShaId treeId = SnapshotTreeStorage.write(newSnapshot);

        // creating new commit and writing it
        String curBranchName = BranchStorage.readCurBranchName();
        Branch curBranch = BranchStorage.read(curBranchName);

        Commit commit = new Commit();
        commit.setMsg(msg);
        commit.setParentCommitsIds(Lists.newArrayList(curBranch.getCurCommitId()));
        commit.setDirTreeId(treeId);
        ShaId commitId = CommitStorage.write(commit);

        // Updating branch
        Branch updBranch = new Branch();
        if (!curBranch.getHeadCommitId().equals(curBranch.getCurCommitId())) {
            // creating new branch if needed
            String brName = "detach_" + curBranch.getCurCommitId();
            updBranch.setName(brName);
        }
        updBranch.setCurCommitId(commitId);
        updBranch.setHeadCommitId(commitId);
        BranchStorage.write(updBranch);
        // update branch if needed
        if (!updBranch.getName().equals(curBranchName)) {
            BranchStorage.writeBranchAsCurrent(updBranch.getName());
        }

        // Storing new Index
        IndexStorage.writeIndex(newIndex);

        // Update log
        Log log = LogStorage.readLog(updBranch.getName());
        log.add(new Log.Entry(commitId, msg));
        LogStorage.write(log);


    }
}
