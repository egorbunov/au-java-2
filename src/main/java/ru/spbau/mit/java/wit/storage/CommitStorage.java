package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.ShaId;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class CommitStorage extends ObjStorage<Commit> {

    @Override
    ShaId write(Commit c) {
        return null;
    }

    @Override
    Commit read(ShaId id) {
        return null;
    }
}
