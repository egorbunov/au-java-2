package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "checkout", description = "Switch to branch or revision")
public class Checkout implements Runnable {
    @Override
    public void run() {
        System.out.println("CHECKOUT; IMPLEMENT ME");
    }
}
