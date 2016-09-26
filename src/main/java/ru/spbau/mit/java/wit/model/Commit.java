package ru.spbau.mit.java.wit.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class Commit {
    public final List<Id> parentCommitsIds;
    public final Id dirTreeId;
    public final String msg;

    /**
     * @param parentCommitsIds parent commits ids
     * @param dirTreeId id of tree entry (SnapshotTree)
     * @param msg commit message
     */
    public Commit(List<Id> parentCommitsIds, Id dirTreeId, String msg) {
        this.parentCommitsIds = Collections.unmodifiableList(parentCommitsIds);
        this.dirTreeId = dirTreeId;
        this.msg = msg;
    }
}
