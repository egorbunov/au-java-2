package ru.spbau.mit.java.wit;

import ru.spbau.mit.java.wit.storage.WitStorage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
public interface WitCommand {
    public abstract int run(Path baseDir, WitStorage storage);
}
