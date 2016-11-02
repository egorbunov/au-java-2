package ru.spbau.mit.java.wit;

import io.airlift.airline.Cli;
import io.airlift.airline.ParseException;
import ru.spbau.mit.java.wit.command.*;
import ru.spbau.mit.java.wit.command.except.*;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
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
                        WitMerge.class,
                        WitStatus.class,
                        WitClean.class,
                        WitReset.class,
                        WitRm.class
                );
        Cli<WitCommand> parser = builder.build();

        // parsing command
        WitCommand cmd;
        try {
            cmd = parser.parse(args);
        } catch (ParseException e) {
            System.out.println("FATAL: " + e.getMessage());
            return;
        }

        // trying to find already initialized repository
        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path witRoot;
        try {
            witRoot = WitInit.findRepositoryRoot(baseDir);
        } catch (IOException e) {
            System.err.println("FATAL: Can't scan for repository root");
            return;
        }

        WitStorage storage = null;
        if (witRoot != null) {
            storage = new WitStorage(witRoot);
        }

        // in case repository not found and command is not useful without it
        // print error
        if (storage == null && !(cmd instanceof WitInit) && !(cmd instanceof WitHelp)) {
            System.err.println("FATAL: can't find wit repository near " + baseDir);
            return;
        }

        // executing command
        try {
            cmd.execute(baseDir, storage);
        } catch (IOException e) {
            System.err.println("FATAL: Repository write/read failed!");
            e.printStackTrace();
        } catch (BranchNotFound e) {
            System.err.println("FATAL: Branch not found: " + e.getBranchName());
        } catch (BranchNotSpecified e) {
            System.err.println("FATAL: Branch not specified");
        } catch (CommitNotFound e) {
            System.err.println("FATAL: Commit not found: " + e.getMessage());
        } catch (FileIsProhibitedForControl e) {
            System.err.println("FATAL: File is outside repository or/and prohibited " +
                    "for control: " + e.getFilename());
        } catch (MergingNotFinished e) {
            System.err.println("FATAL: Finish merge before evaluating command");
        } catch (MessageIsEmpty e) {
            System.err.println("FATAL: Message is empty");
        } catch (NoBaseCommitForBranch e) {
            System.err.println("FATAL: No commit to base branch on");
        } catch (NotAllChangesCommitted e) {
            System.err.println("FATAL: There are not committed, but staged changes");
        } catch (TooShortIdPrefix e) {
            System.err.println("FATAL: Too short commit id prefix, can't determine commit");
        } catch (WitRepoAlreadyExists e) {
            System.err.println("FATAL: Wit repository is already initialized at: " + e.getPath());
        }
    }
}
