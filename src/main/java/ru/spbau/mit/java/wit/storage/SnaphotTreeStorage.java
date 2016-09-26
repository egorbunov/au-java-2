package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.ShaId;
import ru.spbau.mit.java.wit.model.SnapshotTree;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class SnaphotTreeStorage extends ObjStorage<SnapshotTree> {

    @Override
    public SnapshotTree read(ShaId id) {
        return null;
    }

    @Override
    public ShaId write(SnapshotTree tree) {
        return null;
    }
}
