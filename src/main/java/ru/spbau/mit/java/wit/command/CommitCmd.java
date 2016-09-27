package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.SnapshotTree;
import ru.spbau.mit.java.wit.storage.BranchStorage;
import ru.spbau.mit.java.wit.storage.IndexStorage;
import ru.spbau.mit.java.wit.storage.SnapshotTreeStorage;
import ru.spbau.mit.java.wit.storage.WitRepo;

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
        if (msg.isEmpty()) {
            System.err.println("Error: Empty message");
            return;
        }

        if (WitRepo.findRepositoryRoot() == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        Index index = IndexStorage.readIndex();
        Branch curBranch = BranchStorage.read(BranchStorage.readCurBranchName());
        SnapshotTree oldSnapshot = SnapshotTreeStorage.read(curBranch.getHeadCommitId());
        SnapshotTree newSnapshot = new SnapshotTree();


        for (Index.Entry e : index.getEntries()) {

        }
    }
}
