package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.id.IdCommit;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitLogUtils;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "log", description = "List commit history for branch or branches, if many branches specified when " +
                                     "histories are merged accordingly to commit dates")
public class WitLog implements WitCommand {
    @Arguments(description = "branches names")
    private List<String> branchNames;

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        if (branchNames == null || branchNames.isEmpty()) {
            branchNames = Collections.singletonList(storage.readCurBranchName());
        }

        for (String branchName : branchNames) {
            if (storage.readBranch(branchName) == null) {
                System.err.println("Error: no such branch [ " + branchName + " ]");
                return -1;
            }
        }

        List<IdCommit> log;

        if (branchNames.size() == 1) {
            log = WitLogUtils.readBranchLog(branchNames.get(0), storage);
        } else {
            log = WitLogUtils.mergeBranchHistories(branchNames, storage);
        }

        for (IdCommit idCommit : log) {
            Commit c = idCommit.commit;
            System.out.println("Commit: " + idCommit.id);
            System.out.println("Date: " + new Date(c.getTimestamp()).toString());
            System.out.println("Message: " + c.getMsg());
            System.out.println();
        }

        return 0;
    }
}
