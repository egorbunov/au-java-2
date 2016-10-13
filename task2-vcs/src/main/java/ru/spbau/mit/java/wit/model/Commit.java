package ru.spbau.mit.java.wit.model;

import ru.spbau.mit.java.wit.model.id.ShaId;

import java.util.Arrays;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class Commit {
    private List<ShaId> parentCommitsIds;
    private ShaId snapshotId;
    private String msg;
    private long timestamp;

    public Commit() {}

    public Commit(String msg, ShaId snapshotId, long timestamp) {
        this.msg = msg;
        this.snapshotId = snapshotId;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (!Commit.class.isInstance(obj)) {
            return false;
        }
        Commit c = (Commit) obj;
        return Arrays.equals(parentCommitsIds.toArray(), c.parentCommitsIds.toArray()) &&
                snapshotId.equals(c.snapshotId) &&
                msg.equals(c.msg) &&
                timestamp == c.timestamp;
    }

    public List<ShaId> getParentCommitsIds() {
        return parentCommitsIds;
    }

    public void setParentCommitsIds(List<ShaId> parentCommitsIds) {
        this.parentCommitsIds = parentCommitsIds;
    }

    public ShaId getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(ShaId snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
