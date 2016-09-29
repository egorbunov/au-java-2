package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Log;
import ru.spbau.mit.java.wit.storage.WitRepo;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "log", description = "List current branch commits")
public class LogCmd implements Runnable {
    @Arguments(description = "branch name")
    String branchName;

    @Override
    public void run() {
        if (WitRepo.findRepositoryRoot() == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        if (branchName.isEmpty()) {
            branchName = BranchStorage.readCurBranchName();
        }

        Log log = LogStorage.readLog(branchName);

        for (Log.Entry e : log.getEntries()) {
            System.out.println(e.commitId + " | " + e.msg);
        }
    }
}
