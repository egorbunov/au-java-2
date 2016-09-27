package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "merge", description = "MergeCmd some branch to currently active")
public class MergeCmd implements Runnable {
    @Arguments(description = "Branch, which will be merged into active branch")
    String branch;

    @Override
    public void run() {
        System.out.println("MERGE; IMPLEMENT ME");
    }
}
