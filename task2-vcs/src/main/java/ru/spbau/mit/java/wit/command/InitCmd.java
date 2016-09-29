package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.storage.WitRepo;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "init", description = "initializes empty wit repository")
public class InitCmd implements Runnable {
    @Override
    public void run() {
        Path baseDir = Paths.get(System.getProperty("user.dir"));

        // checking if repository is already initialized
        Path repoRoot = WitRepo.findRepositoryRoot(baseDir);
        if (repoRoot != null) {
            System.out.println("Wit Repository is already initialized under" +
                    repoRoot.toString());
            return;
        }

        // initializing new repository
        try {
            WitRepo.init(baseDir);
        } catch (Exception e) { // TODO: change exception
            System.err.println("ERROR: Can't initialize repository");
        }
    }
}
