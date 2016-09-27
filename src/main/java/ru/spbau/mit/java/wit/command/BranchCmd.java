package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.storage.BranchStorage;
import ru.spbau.mit.java.wit.storage.WitRepo;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "branch", description = "Create new branch starting and current commit")
public class BranchCmd implements Runnable {
    @Arguments(description = "Name of branch to be created")
    String branchName;

    @Override
    public void run() {
        if (WitRepo.findRepositoryRoot() == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        if (BranchStorage.read(branchName) != null) {
            System.err.println("Error: Branch with name " + branchName + " already exists");
            return;
        }

        String curBranchName = BranchStorage.readCurBranchName();
        Branch curBranch = BranchStorage.read(curBranchName);

        BranchStorage.write(new Branch(branchName, curBranch.getCurCommitId(),
                curBranch.getCurCommitId()));
    }
}
