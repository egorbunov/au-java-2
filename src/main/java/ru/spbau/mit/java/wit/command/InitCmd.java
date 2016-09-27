package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.Wit;
import ru.spbau.mit.java.wit.storage.WitRepo;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "init", description = "initializes empty wit repository")
public class InitCmd implements Runnable {
    @Override
    public void run() {
        System.out.println("INIT");

        // checking if repository is already initialized
        Path repoRoot = WitRepo.findStorageRoot();
        if (repoRoot != null) {
            System.out.println("Wit Repository is already initialized under" +
                    repoRoot.toString());
            return;
        }

        // initializing new repository
        try {
            WitRepo.init();
        } catch (Exception e) { // TODO: change exception
            System.err.println("ERROR: Can't initialize repository");
        }
    }
}
