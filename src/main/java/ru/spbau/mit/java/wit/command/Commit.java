package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "commit", description = "Commit snapshot to vcs")
public class Commit implements Runnable {
    @Override
    public void run() {
        System.out.println("COMMIT; IMPLEMENT ME");
    }
}
