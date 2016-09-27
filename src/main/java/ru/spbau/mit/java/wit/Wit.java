package ru.spbau.mit.java.wit;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import ru.spbau.mit.java.wit.command.*;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class Wit {
    public static void main(String[] args) {
        @SuppressWarnings("unchecked")
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("wit")
                .withDescription("Dead simple VCS")
                .withDefaultCommand(Help.class)
                .withCommands(
                        Help.class,
                        AddCmd.class,
                        CheckoutCmd.class,
                        BranchCmd.class,
                        CommitCmd.class,
                        InitCmd.class,
                        LogCmd.class,
                        MergeCmd.class
                );
        Cli<Runnable> parser = builder.build();
        parser.parse(args).run();
    }
}
