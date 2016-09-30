package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.WitCommand;
import ru.spbau.mit.java.wit.storage.WitStorage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "merge", description = "MergeCmd some branch to currently active")
public class MergeCmd implements WitCommand {
    @Arguments(description = "Branch, which will be merged into active branch")
    String branch;

    @Override
    public int run(Path baseDir, WitStorage storage) {
        System.out.println("IMPLEMENT MERGE =)");

        return 0;
    }
}
