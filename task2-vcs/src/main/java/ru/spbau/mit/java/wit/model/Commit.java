package ru.spbau.mit.java.wit.model;

import ru.spbau.mit.java.wit.model.id.ShaId;

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
}
