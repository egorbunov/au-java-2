package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "merge", description = "Merge some branch to currently active")
public class Merge implements Runnable {
    @Override
    public void run() {
        System.out.println("MERGE; IMPLEMENT ME");
    }
}
