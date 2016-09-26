package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "add", description = "AddCmd files to be managed by vcs")
public class AddCmd implements Runnable {
    @Override
    public void run() {
        System.out.println("ADD");
    }
}
