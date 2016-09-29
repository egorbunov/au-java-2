package ru.spbau.mit.java.wit.model;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class Log extends AbstractCollection<Log.Entry> {
    public static class Entry {
        public final ShaId commitId;
        public final String msg;

        public Entry(ShaId commitId, String msg) {
            this.commitId = commitId;
            this.msg = msg;
        }
    }

    private ArrayList<Entry> log;

    @Override
    public boolean add(Entry entry) {
        log.add(entry);
        return true;
    }

    @Override
    public Iterator<Entry> iterator() {
        return log.iterator();
    }

    @Override
    public int size() {
        return log.size();
    }

    @Override
    public void forEach(Consumer<? super Entry> action) {
        log.forEach(action);
    }

    @Override
    public Spliterator<Entry> spliterator() {
        return log.spliterator();
    }
}
