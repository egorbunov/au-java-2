package ru.spbau.mit.java.wit;

import io.airlift.airline.Cli;
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
                        Add.class,
                        Checkout.class,
                        Branch.class,
                        Commit.class,
                        Init.class,
                        Log.class,
                        Merge.class
                );
        Cli<Runnable> parser = builder.build();
        parser.parse(args).run();
    }
}
