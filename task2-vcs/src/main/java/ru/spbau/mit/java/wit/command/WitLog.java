package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "log", description = "List current branch commits")
public class WitLog implements WitCommand {
    @Arguments(description = "branch name")
    String branchName;

    @Override
    public int execute(Path workingDir, WitStorage storage) {
        if (branchName.isEmpty()) {
            branchName = storage.readCurBranchName();
        }

        List<ShaId> log = storage.readCommitLog(branchName);

        for (ShaId id : log) {
            Commit c = storage.readCommit(id);
            System.out.println("Commit: " + id);
            System.out.println("Date: " + new Date(c.getTimestamp()).toString());
            System.out.println("Message: " + c.getMsg());
            System.out.println();
        }

        return 0;
    }
}
