package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "merge", description = "WitMerge some branch to currently active")
public class WitMerge implements WitCommand {
    @Arguments(description = "Branch, which will be merged into active branch")
    String branch;

    @Override
    public int execute(Path workingDir, WitStorage storage) {
        System.out.println("IMPLEMENT MERGE =)");

        return 0;
    }
}
