package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "init", description = "initializes empty wit repository")
public class Init implements Runnable {
    @Override
    public void run() {
        System.out.println("INIT; Implement me");
    }
}
