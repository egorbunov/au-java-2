package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.WitCommand;
import ru.spbau.mit.java.wit.storage.WitInit;
import ru.spbau.mit.java.wit.storage.WitStorage;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "init", description = "initializes empty wit repository")
public class InitCmd implements WitCommand {
    @Override
    public int run(Path baseDir, WitStorage storage) {
        // checking if repository is already initialized
        Path repoRoot = WitInit.findRepositoryRoot(baseDir);
        if (repoRoot != null) {
            System.out.println("Wit Repository is already initialized under" +
                    repoRoot.toString());
            return -1;
        }
        // initializing new repository
        try {
            WitInit.init(baseDir);
        } catch (Exception e) { // TODO: change exception
            e.printStackTrace();
            System.err.println("ERROR: Can't initialize repository");
        }

        return 0;
    }
}
