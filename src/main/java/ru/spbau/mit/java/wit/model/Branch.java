package ru.spbau.mit.java.wit.model;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class Branch {
    private String branch;
    private ShaId headCommitId;

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public ShaId getHeadCommitId() {
        return headCommitId;
    }

    public void setHeadCommitId(ShaId headCommitId) {
        this.headCommitId = headCommitId;
    }
}
