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
    @Arguments(description = "Name of branches to be created")
    private List<String> branchNames;

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        if (branchNames == null || branchNames.isEmpty()) {
            printBranches(storage);
            return 0;
        }

        String curBranchName = storage.readCurBranchName();
        Branch curBranch = storage.readBranch(curBranchName);
        for (String branchName : branchNames) {
            if (storage.readBranch(branchName) != null) {
                System.err.println("Error: Branch with name " + branchName + " already exists");
                continue;
            }
            if (curBranch.getHeadCommitId().equals(ShaId.EmptyId)) {
                System.out.println("FATAL: can't branch, no commit to base branch on;");
                return -1;
            }
            createOneBranch(branchName, curBranch, storage);
        }

        return 0;
    }

    private static void printBranches(WitStorage storage) throws IOException {
        String curBranchName = storage.readCurBranchName();
        Branch curBranch = storage.readBranch(curBranchName);
        List<Branch> branches = storage.readAllBranches();
        for (Branch b : branches) {
            if (b.getName().equals(curBranch.getName())) {
                System.out.println("* " + b.getName());
            } else {
                System.out.println("  " + b.getName());
            }
        }
    }

    private static void createOneBranch(String branchName, Branch curBranch, WitStorage storage) throws IOException {
        // creating log file, for newly created branch
        // it is equal to current branch log, always
        List<ShaId> commitLog = storage.readCommitLog(curBranch.getName());
        storage.writeCommitLog(commitLog, branchName);
        storage.writeBranch(new Branch(branchName, curBranch.getHeadCommitId()));
    }

    public void setNewBranchNames(List<String> branchNames) {
        this.branchNames = branchNames;
    }
}
