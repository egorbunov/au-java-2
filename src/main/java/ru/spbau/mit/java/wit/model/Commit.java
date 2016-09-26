package ru.spbau.mit.java.wit.model;

import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class Commit {
    private List<ShaId> parentCommitsIds;
    private ShaId dirTreeId;
    private String msg;

    public List<ShaId> getParentCommitsIds() {
        return parentCommitsIds;
    }

    public void setParentCommitsIds(List<ShaId> parentCommitsIds) {
        this.parentCommitsIds = parentCommitsIds;
    }

    public ShaId getDirTreeId() {
        return dirTreeId;
    }

    public void setDirTreeId(ShaId dirTreeId) {
        this.dirTreeId = dirTreeId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
