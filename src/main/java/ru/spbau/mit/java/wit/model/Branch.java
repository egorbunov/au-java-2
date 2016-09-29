package ru.spbau.mit.java.wit.model;

import ru.spbau.mit.java.wit.model.id.ShaId;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class Branch {
    private String name;
    private ShaId headCommitId;
    private ShaId curCommitId;

    public Branch() {}

    public Branch(String name, ShaId headCommitId, ShaId curCommitId) {
        this.name = name;
        this.headCommitId = headCommitId;
        this.curCommitId = curCommitId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ShaId getHeadCommitId() {
        return headCommitId;
    }

    public void setHeadCommitId(ShaId headCommitId) {
        this.headCommitId = headCommitId;
    }

    public ShaId getCurCommitId() {
        return curCommitId;
    }

    public void setCurCommitId(ShaId curCommitId) {
        this.curCommitId = curCommitId;
    }
}
