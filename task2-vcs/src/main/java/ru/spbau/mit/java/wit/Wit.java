package ru.spbau.mit.java.wit;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import ru.spbau.mit.java.wit.command.*;
import ru.spbau.mit.java.wit.storage.WitInit;
import ru.spbau.mit.java.wit.storage.WitPaths;
import ru.spbau.mit.java.wit.storage.WitStorage;

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
                        AddCmd.class,
                        CheckoutCmd.class,
                        BranchCmd.class,
                        CommitCmd.class,
                        InitCmd.class,
                        LogCmd.class,
                        MergeCmd.class
                );
        Cli<WitCommand> parser = builder.build();
        WitCommand cmd = parser.parse(args);

        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path witRoot = WitInit.findRepositoryRoot(baseDir);
        WitStorage storage = null;
        if (witRoot != null) {
            storage = new WitStorage(witRoot);
        }

        cmd.run(baseDir, storage);
    }
}
