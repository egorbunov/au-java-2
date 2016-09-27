package ru.spbau.mit.java.wit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class Log {
    public static class Entry {
        public final ShaId commitId;
        public final String msg;

        public Entry(ShaId commitId, String msg) {
            this.commitId = commitId;
            this.msg = msg;
        }
    }

    public Log(String branchName) {
        this.branchName = branchName;
    }

    private ArrayList<Entry> log;
    private final String branchName;

    public void add(Entry entry) {
        log.add(entry);
    }
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(log);
    }
}
