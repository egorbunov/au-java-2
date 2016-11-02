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

    public Branch() {}

    @Override
    public boolean equals(Object obj) {
        if (!Branch.class.isInstance(obj)) {
            return false;
        }
        Branch b = (Branch) obj;
        return b.name.equals(name) &&
                b.headCommitId.equals(headCommitId);
    }

    public Branch(String name, ShaId headCommitId) {
        this.name = name;
        this.headCommitId = headCommitId;
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
}
