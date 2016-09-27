package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.ShaId;

import java.util.Set;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class CommitStorage {
    private CommitStorage() {}

    public static ShaId write(Commit c) {
        return null;
    }

    public static Commit read(ShaId id) {
        return null;
    }

    public static ShaId resolveCommitByPrefix(String prefix) {
        return ShaId.EmptyId; // TODO: fixme
    }
}
