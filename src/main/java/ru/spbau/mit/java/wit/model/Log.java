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
    public class Entry {
        public final ShaId commitId;
        public final String branch;

        public Entry(ShaId commitId, String branch) {
            this.commitId = commitId;
            this.branch = branch;
        }
    }

    ArrayList<Entry> log;

    void add(Entry entry) {
        log.add(entry);
    }

    List<Entry> getEntries() {
        return Collections.unmodifiableList(log);
    }
}
