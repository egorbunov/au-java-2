package ru.spbau.mit.java.wit.command;

import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
public interface WitCommand {

    /**
     *
     * @param workingDir working directory, which is controlled (or going to be controlled) by VCS
     * @param storage repository storage, which takes version control care of files at {@code baseDir}
     * @return exit code: 0 for success and non-zero value to designate error
     */
    int execute(Path workingDir, WitStorage storage);
}
