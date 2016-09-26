package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "branch", description = "Create new branch starting and current commit")
public class BranchCmd implements Runnable {
    @Override
    public void run() {
        System.out.println("BRANCH; IMPLEMENT ME");
    }
}
