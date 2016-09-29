package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.storage.WitPaths;
import ru.spbau.mit.java.wit.storage.WitRepo;
import ru.spbau.mit.java.wit.storage.WitStorage;
import ru.spbau.mit.java.wit.storage.io.StoreUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path witRoot = WitRepo.findRepositoryRoot(baseDir);

        if (witRoot == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        WitStorage storage = new WitStorage(witRoot);

        if (Files.exists(WitPaths.getBranchInfoFilePath(witRoot, branchName))) {
            System.err.println("Error: Branch with name " + branchName + " already exists");
            return;
        }

        Branch curBranch;
        try {
            String curBranchName = storage.readCurBranchName();
            curBranch = storage.readBranch(curBranchName);
        } catch (IOException e) {
            System.err.println("Error: Can't read current branch");
            e.printStackTrace();
            return;
        }

        try {
            storage.writeBranch(new Branch(branchName, curBranch.getCurCommitId(),
                    curBranch.getCurCommitId()));
        } catch (IOException e) {
            System.err.println("Error: Can't create new branch");
            e.printStackTrace();
            return;
        }
    }
}
