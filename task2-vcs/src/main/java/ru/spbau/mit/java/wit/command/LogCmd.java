package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.WitCommand;
import ru.spbau.mit.java.wit.model.Log;
import ru.spbau.mit.java.wit.storage.WitInit;
import ru.spbau.mit.java.wit.storage.WitStorage;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "log", description = "List current branch commits")
public class LogCmd implements WitCommand {
    @Arguments(description = "branch name")
    String branchName;

    @Override
    public int run(Path baseDir, WitStorage storage) {
        if (branchName.isEmpty()) {
            branchName = storage.readCurBranchName();
        }

        Log log = storage.readLog(branchName);

        for (Log.Entry e : log) {
            System.out.println(e.commitId + " | " + e.msg);
        }

        return 0;
    }
}
