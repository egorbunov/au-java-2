package ru.spbau.mit.java.wit.model.id;

import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.repository.WitLogUtils;

/**
 * We can't store sha id in commit file because it's name
 * based on commit contents.
 *
 * So this class represents commit with it's attached id
 */
public class IdCommit {
    public final ShaId id;
    public final Commit commit;

    public IdCommit(ShaId id, Commit commit) {
        this.id = id;
        this.commit = commit;
    }

    @Override
    public boolean equals(Object obj) {
        if (!IdCommit.class.isInstance(obj)) {
            return false;
        }
        IdCommit idc = (IdCommit) obj;
        return idc.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
