package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "branch", description = "Create new branch starting and current commit")
public class WitBranch implements WitCommand {
    @Arguments(description = "Name of branch to be created")
    private String branchName;


    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        if (storage.readBranch(branchName) != null) {
            System.err.println("Error: Branch with name " + branchName + " already exists");
            return -1;
        }

        Branch curBranch;
        String curBranchName = storage.readCurBranchName();
        curBranch = storage.readBranch(curBranchName);

        if (curBranch.getHeadCommitId().equals(ShaId.EmptyId)) {
            System.out.println("FATAL: can't branch, no commit to base branch on;");
            return -1;
        }

        // creating log file, for newly created branch
        // it is equal to current branch log, always
        List<ShaId> commitLog = storage.readCommitLog(curBranchName);
        storage.writeCommitLog(commitLog, branchName);

        storage.writeBranch(new Branch(branchName, curBranch.getHeadCommitId()));

        return 0;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
