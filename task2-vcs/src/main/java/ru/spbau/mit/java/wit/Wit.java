package ru.spbau.mit.java.wit;

import io.airlift.airline.Cli;
import ru.spbau.mit.java.wit.command.*;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class Wit {
    public static void main(String[] args) {
        @SuppressWarnings("unchecked")
        Cli.CliBuilder<WitCommand> builder = Cli.<WitCommand>builder("wit")
                .withDescription("Dead simple VCS")
                .withDefaultCommand(WitHelp.class)
                .withCommands(
                        WitHelp.class,
                        WitAdd.class,
                        WitCheckout.class,
                        WitBranch.class,
                        WitCommit.class,
                        WitInit.class,
                        WitLog.class,
                        WitMerge.class
                );
        Cli<WitCommand> parser = builder.build();
        WitCommand cmd = parser.parse(args);

        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path witRoot = WitInit.findRepositoryRoot(baseDir);
        WitStorage storage = null;
        if (witRoot != null) {
            storage = new WitStorage(witRoot);
        }

        try {
            cmd.execute(baseDir, storage);
        } catch (WitStorage.StorageException e) {
            System.err.println("FATAL: repository write/read failed!");
        }
    }
}
