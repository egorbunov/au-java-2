package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import io.airlift.airline.Help;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import javax.inject.Inject;
import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "help", description = "Help")
public class WitHelp implements WitCommand {
    @Inject
    private Help help;

    @Override
    public int execute(Path workingDir, WitStorage storage) {
        help.run();
        return 0;
    }
}
