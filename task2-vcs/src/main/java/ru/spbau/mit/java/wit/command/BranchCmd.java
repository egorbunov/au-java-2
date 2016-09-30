package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.WitCommand;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.storage.WitStorage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "branch", description = "Create new branch starting and current commit")
public class BranchCmd implements WitCommand {
    @Arguments(description = "Name of branch to be created")
    String branchName;

    @Override
    public int run(Path baseDir, WitStorage storage) {
        if (storage.readBranch(branchName) != null) {
            System.err.println("Error: Branch with name " + branchName + " already exists");
            return -1;
        }

        Branch curBranch;
        String curBranchName = storage.readCurBranchName();
        curBranch = storage.readBranch(curBranchName);

        storage.writeBranch(new Branch(branchName, curBranch.getCurCommitId(),
                curBranch.getCurCommitId()));

        return 0;
    }
}
