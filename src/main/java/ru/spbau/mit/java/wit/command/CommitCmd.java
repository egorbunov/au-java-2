package ru.spbau.mit.java.wit.command;

import com.google.common.collect.Lists;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import ru.spbau.mit.java.wit.model.*;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.storage.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path witRoot = WitRepo.findRepositoryRoot(baseDir);
        if (witRoot == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        WitStorage storage = new WitStorage(witRoot);

        if (msg.isEmpty()) {
            System.err.println("Error: Empty message");
            return;
        }

        // Creating new Snapshot tree and new Index
        Index index;
        try {
            index = storage.readIndex();
        } catch (IOException e) {
            System.err.println("Can't read index");
            e.printStackTrace();
            return;
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
        try {
            treeId = storage.writeSnapshot(newSnapshot);
        } catch (IOException e) {
            System.err.println("Error: Can't write snapshot!");
            e.printStackTrace();
            return;
        }

        // creating new commit and writing it
        Branch curBranch;
        try {
            String curBranchName = storage.readCurBranchName();
            curBranch = storage.readBranch(curBranchName);
        } catch (IOException e) {
            System.err.println("Error: can't read cur branch");
            e.printStackTrace();
            return;
        }

        Commit commit = new Commit();
        commit.setMsg(msg);
        commit.setParentCommitsIds(Lists.newArrayList(curBranch.getCurCommitId()));
        commit.setSnapshotId(treeId);


        ShaId commitId;
        try {
            commitId = storage.writeCommit(commit);
        } catch (IOException e) {
            System.err.println("Error: can't write new commit");
            e.printStackTrace();
            return;
        }

        // Updating branch
        Branch updBranch = new Branch();
        if (!curBranch.getHeadCommitId().equals(curBranch.getCurCommitId())) {
            // creating new branch if needed
            String brName = "detach_" + curBranch.getCurCommitId();
            updBranch.setName(brName);
        }
        updBranch.setCurCommitId(commitId);
        updBranch.setHeadCommitId(commitId);

        // updating branches data
        try {
            storage.writeBranch(updBranch);
            // update branch if needed
            if (!updBranch.getName().equals(curBranch.getName())) {
                storage.writeCurBranchName(updBranch.getName());
            }
        } catch (IOException e) {
            System.err.println("Error: can't update branch data");
            e.printStackTrace();
            return;
        }

        // Storing new Index
        try {
            storage.writeIndex(newIndex);
        } catch (IOException e) {
            System.err.println("Error: can't write new index");
            e.printStackTrace();
            return;
        }

        // Update log
        try {
            Log log = storage.readLog(updBranch.getName());
            log.add(new Log.Entry(commitId, msg));
            storage.writeLog(log, updBranch.getName());
        } catch (IOException e) {
            System.err.println("Error: can't update log");
            e.printStackTrace();
        }
    }
}
